package com.formulamanager.sokker.persistence;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Team-owned Asistente player and junior records. Player identity is (tid,pid). */
public final class TeamPlayerDAO {
    private static final Pattern PATH = Pattern.compile("^(?:backup/)?([0-9]+)(?:_([0-9]+))?(?:(_historico)(_juveniles)?|(_juveniles)(_historico)?)?\\.properties$");
    private static final Pattern PID = Pattern.compile("[0-9]+");
    private final SqliteDatabase database;

    public TeamPlayerDAO(SqliteDatabase database) {
        if(database==null || !database.isPlayerGroupsEnabled()) throw new IllegalArgumentException("Asistente database required");
        this.database=database;
    }

    public static boolean isManagedPath(String path) {
        return path!=null && PATH.matcher(path.replace('\\','/')).matches();
    }

    public void save(int tid,String recordType,int snapshotRound,Map<Integer,String> players) throws SQLException {
        validate(tid,recordType,snapshotRound);
        try(Connection c=database.openConnection()) {
            c.setAutoCommit(false);
            try {
                try(PreparedStatement d=c.prepareStatement(
                        "DELETE FROM team_player WHERE tid=? AND record_type=? AND snapshot_round=?")) {
                    d.setInt(1,tid); d.setString(2,recordType); d.setInt(3,snapshotRound); d.executeUpdate();
                }
                try(PreparedStatement i=c.prepareStatement(
                        "INSERT INTO team_player(tid,pid,record_type,snapshot_round,serialized_data) VALUES(?,?,?,?,?)")) {
                    for(Map.Entry<Integer,String> player:players.entrySet()) {
                        if(player.getKey()==null || player.getKey().intValue()<=0 || player.getValue()==null)
                            throw new IllegalArgumentException("Invalid player record");
                        i.setInt(1,tid); i.setInt(2,player.getKey()); i.setString(3,recordType);
                        i.setInt(4,snapshotRound); i.setString(5,player.getValue()); i.addBatch();
                    }
                    i.executeBatch();
                }
                c.commit();
            } catch(Exception e) {
                c.rollback();
                if(e instanceof SQLException)throw (SQLException)e;
                if(e instanceof IllegalArgumentException)throw (IllegalArgumentException)e;
                throw new SQLException("Cannot save team players for tid "+tid,e);
            } finally { c.setAutoCommit(true); }
        }
    }

    public Map<Integer,String> load(int tid,String recordType,int snapshotRound) throws SQLException {
        validate(tid,recordType,snapshotRound);
        try(Connection c=database.openConnection();
            PreparedStatement q=c.prepareStatement(
                "SELECT pid,serialized_data FROM team_player WHERE tid=? AND record_type=? AND snapshot_round=? ORDER BY pid")) {
            q.setInt(1,tid); q.setString(2,recordType); q.setInt(3,snapshotRound);
            Map<Integer,String> records=new LinkedHashMap<Integer,String>();
            try(ResultSet r=q.executeQuery()){while(r.next())records.put(r.getInt(1),r.getString(2));}
            return Collections.unmodifiableMap(records);
        }
    }

    public List<String> listBackupFiles(int tid) throws SQLException {
        if (tid <= 0) throw new IllegalArgumentException("tid must be positive");
        List<String> paths = new ArrayList<String>();
        String sql = "SELECT DISTINCT record_type,snapshot_round FROM team_player WHERE tid=? AND snapshot_round>0 " +
                "UNION SELECT DISTINCT record_type,snapshot_round FROM team_player_extension WHERE tid=? AND snapshot_round>0 " +
                "ORDER BY snapshot_round,record_type";
        try (Connection c = database.openConnection(); PreparedStatement q = c.prepareStatement(sql)) {
            q.setInt(1, tid); q.setInt(2, tid);
            try (ResultSet r = q.executeQuery()) {
                while (r.next()) {
                    String type = r.getString(1);
                    int round = r.getInt(2);
                    String suffix = "PLAYER".equals(type) ? ""
                            : "PLAYER_HISTORY".equals(type) ? "_historico"
                            : "JUVENILE".equals(type) ? "_juveniles" : "_historico_juveniles";
                    paths.add(tid + "_" + round + suffix + ".properties");
                }
            }
        }
        return Collections.unmodifiableList(paths);
    }

    public void deleteTeam(int tid) throws SQLException {
        if (tid <= 0) throw new IllegalArgumentException("tid must be positive");
        try (Connection c = database.openConnection()) {
            c.setAutoCommit(false);
            try {
                String[] tables = {"team_player", "team_player_extension", "player_group_assignment"};
                for (String table : tables) {
                    try (PreparedStatement d = c.prepareStatement("DELETE FROM " + table + " WHERE tid=?")) {
                        d.setInt(1, tid);
                        d.executeUpdate();
                    }
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public String load(int tid,int pid,String recordType) throws SQLException {
        validate(tid,recordType,0);
        try(Connection c=database.openConnection();
            PreparedStatement q=c.prepareStatement(
                "SELECT serialized_data FROM team_player WHERE tid=? AND pid=? AND record_type=? AND snapshot_round=0")) {
            q.setInt(1,tid);q.setInt(2,pid);q.setString(3,recordType);
            try(ResultSet r=q.executeQuery()){return r.next()?r.getString(1):null;}
        }
    }

    /** Imports every existing team player/junior file and removes it only after commit. */
    public void migrateLegacy(SqlitePropertiesStore legacy,File root) throws IOException,SQLException {
        List<String> documents=new ArrayList<String>();
        for(String path:legacy.list(null,null))if(isManagedPath(path))documents.add(path);
        try(Connection c=database.openConnection()) {
            c.setAutoCommit(false);
            try {
                boolean done;
                try(PreparedStatement q=c.prepareStatement("SELECT 1 FROM migration_marker WHERE name=?")) {
                    q.setString(1,"team_players_legacy_imported");
                    try(ResultSet r=q.executeQuery()){done=r.next();}
                }
                for(String path:documents) {
                    if(!done)migrateDocument(c,path,legacy.load(path));
                    try(PreparedStatement d=c.prepareStatement("DELETE FROM legacy_properties_file WHERE path=?")) {
                        d.setString(1,path);d.executeUpdate();
                    }
                }
                if(!done)try(PreparedStatement i=c.prepareStatement("INSERT INTO migration_marker(name,completed_at) VALUES(?,?)")) {
                    i.setString(1,"team_players_legacy_imported");i.setLong(2,System.currentTimeMillis());i.executeUpdate();
                }
                c.commit();
            } catch(Exception e) {
                c.rollback();
                if(e instanceof IOException)throw (IOException)e;
                if(e instanceof SQLException)throw (SQLException)e;
                throw new SQLException("Team player migration failed",e);
            } finally {c.setAutoCommit(true);}
        }
        for(String path:documents) {
            File source=new File(root,path.replace('/',File.separatorChar));
            if(source.exists()&&!source.delete())throw new IOException("Cannot remove migrated team player file: "+source);
        }
    }

    private void migrateDocument(Connection c,String path,Properties properties) throws SQLException {
        if(properties==null)throw new SQLException("Missing imported team player file: "+path);
        Matcher m=PATH.matcher(path.replace('\\','/'));
        if(!m.matches())return;
        int tid=Integer.parseInt(m.group(1));
        int round=m.group(2)==null?0:Integer.parseInt(m.group(2));
        boolean historical=m.group(3)!=null||m.group(6)!=null,juvenile=m.group(4)!=null||m.group(5)!=null;
        String type=juvenile?(historical?"JUVENILE_HISTORY":"JUVENILE"):(historical?"PLAYER_HISTORY":"PLAYER");
        Map<Integer,String> players=new LinkedHashMap<Integer,String>();
        for(String key:properties.stringPropertyNames()) {
            if(PID.matcher(key).matches()) {
                int pid=Integer.parseInt(key);
                if(pid<=0)throw new SQLException("Invalid player PID in "+path+": "+key);
                players.put(pid,properties.getProperty(key));
            } else {
                try(PreparedStatement extension=c.prepareStatement(
                        "INSERT OR REPLACE INTO team_player_extension(tid,record_type,snapshot_round,property_key,property_value) VALUES(?,?,?,?,?)")) {
                    extension.setInt(1,tid);extension.setString(2,type);extension.setInt(3,round);
                    extension.setString(4,key);extension.setString(5,properties.getProperty(key));extension.executeUpdate();
                }
            }
        }
        try(PreparedStatement insert=c.prepareStatement(
                "INSERT INTO team_player(tid,pid,record_type,snapshot_round,serialized_data) VALUES(?,?,?,?,?)")) {
            for(Map.Entry<Integer,String> player:players.entrySet()) {
                insert.setInt(1,tid);insert.setInt(2,player.getKey());insert.setString(3,type);
                insert.setInt(4,round);insert.setString(5,player.getValue());insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    private static void validate(int tid,String type,int round) {
        if(tid<=0)throw new IllegalArgumentException("tid must be positive");
        if(!"PLAYER".equals(type)&&!"PLAYER_HISTORY".equals(type)&&!"JUVENILE".equals(type)&&!"JUVENILE_HISTORY".equals(type))
            throw new IllegalArgumentException("Invalid team player record type: "+type);
        if(round<0)throw new IllegalArgumentException("snapshot round cannot be negative");
    }
}
