package com.formulamanager.sokker.persistence;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * SQLite persistence for Asistente accounts. The serialized Usuario payload
 * remains the canonical representation of account settings; training rounds
 * and unmanaged property keys are stored separately without discarding them.
 */
public final class UserDAO {
    private final SqliteDatabase database;

    public UserDAO(SqliteDatabase database) {
        if (database == null || !database.isPlayerGroupsEnabled()) {
            throw new IllegalArgumentException("Asistente database required");
        }
        this.database = database;
    }

    public static final class UserRecord {
        private final String serialized;
        private final String notes;
        private final Long deletionRequestedAt;
        private final Map<Integer, String> training;
        private final Properties extensions;
        private final long updatedAt;

        private UserRecord(String serialized, String notes, Long deletionRequestedAt,
                Map<Integer, String> training, Properties extensions, long updatedAt) {
            this.serialized = serialized;
            this.notes = notes;
            this.deletionRequestedAt = deletionRequestedAt;
            this.training = Collections.unmodifiableMap(training);
            this.extensions = extensions;
            this.updatedAt = updatedAt;
        }
        public String getSerialized() { return serialized; }
        public String getNotes() { return notes; }
        public Long getDeletionRequestedAt() { return deletionRequestedAt; }
        public Map<Integer, String> getTraining() { return training; }
        public Properties getExtensions() { return extensions; }
        public long getUpdatedAt() { return updatedAt; }
    }

    public UserRecord load(String login) throws SQLException {
        String normalized = normalizeLogin(login);
        try (Connection c = database.openConnection();
             PreparedStatement q = c.prepareStatement(
                 "SELECT user_id,serialized_data,notes,account_deletion_requested_at,updated_at FROM app_user WHERE login=?")) {
            q.setString(1, normalized);
            try (ResultSet r = q.executeQuery()) {
                if (!r.next()) return null;
                long id = r.getLong("user_id");
                Long deletion = r.getObject("account_deletion_requested_at") == null
                        ? null : Long.valueOf(r.getLong("account_deletion_requested_at"));
                Map<Integer, String> training = new TreeMap<Integer, String>();
                try (PreparedStatement t = c.prepareStatement(
                        "SELECT round_no,serialized_data FROM user_training WHERE user_id=? ORDER BY round_no")) {
                    t.setLong(1, id);
                    try (ResultSet tr = t.executeQuery()) {
                        while (tr.next()) training.put(Integer.valueOf(tr.getInt(1)), tr.getString(2));
                    }
                }
                Properties extensions = new Properties();
                try (PreparedStatement e = c.prepareStatement(
                        "SELECT property_key,property_value FROM user_property_extension WHERE user_id=?")) {
                    e.setLong(1, id);
                    try (ResultSet er = e.executeQuery()) {
                        while (er.next()) extensions.setProperty(er.getString(1), er.getString(2));
                    }
                }
                return new UserRecord(r.getString("serialized_data"), r.getString("notes"),
                        deletion, training, extensions, r.getLong("updated_at"));
            }
        }
    }

    public void save(String login, String serialized, String notes,
            Long deletionRequestedAt, Map<Integer, String> training, Properties extensions)
            throws SQLException {
        String normalized = normalizeLogin(login);
        if (serialized == null) throw new IllegalArgumentException("serialized user is required");
        try (Connection c = database.openConnection()) {
            c.setAutoCommit(false);
            try {
                long id;
                try (PreparedStatement upsert = c.prepareStatement(
                        "INSERT INTO app_user(login,serialized_data,notes,account_deletion_requested_at,updated_at) " +
                        "VALUES(?,?,?,?,?) ON CONFLICT(login) DO UPDATE SET serialized_data=excluded.serialized_data," +
                        "notes=excluded.notes,account_deletion_requested_at=excluded.account_deletion_requested_at," +
                        "updated_at=excluded.updated_at")) {
                    upsert.setString(1, normalized);
                    upsert.setString(2, serialized);
                    upsert.setString(3, notes == null ? "" : notes);
                    if (deletionRequestedAt == null) upsert.setNull(4, java.sql.Types.BIGINT);
                    else upsert.setLong(4, deletionRequestedAt.longValue());
                    upsert.setLong(5, System.currentTimeMillis());
                    upsert.executeUpdate();
                }
                try (PreparedStatement q = c.prepareStatement("SELECT user_id FROM app_user WHERE login=?")) {
                    q.setString(1, normalized);
                    try (ResultSet r = q.executeQuery()) {
                        if (!r.next()) throw new SQLException("User upsert did not produce a row: " + normalized);
                        id = r.getLong(1);
                    }
                }
                try (PreparedStatement d = c.prepareStatement("DELETE FROM user_training WHERE user_id=?")) {
                    d.setLong(1, id); d.executeUpdate();
                }
                if (training != null) try (PreparedStatement t = c.prepareStatement(
                        "INSERT INTO user_training(user_id,round_no,serialized_data) VALUES(?,?,?)")) {
                    for (Map.Entry<Integer, String> entry : training.entrySet()) {
                        if (entry.getKey() == null || entry.getValue() == null)
                            throw new IllegalArgumentException("Invalid training record");
                        t.setLong(1, id); t.setInt(2, entry.getKey().intValue());
                        t.setString(3, entry.getValue()); t.addBatch();
                    }
                    t.executeBatch();
                }
                try (PreparedStatement d = c.prepareStatement("DELETE FROM user_property_extension WHERE user_id=?")) {
                    d.setLong(1, id); d.executeUpdate();
                }
                if (extensions != null) try (PreparedStatement e = c.prepareStatement(
                        "INSERT INTO user_property_extension(user_id,property_key,property_value) VALUES(?,?,?)")) {
                    for (String key : extensions.stringPropertyNames()) {
                        e.setLong(1, id); e.setString(2, key); e.setString(3, extensions.getProperty(key)); e.addBatch();
                    }
                    e.executeBatch();
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                if (e instanceof SQLException) throw (SQLException)e;
                if (e instanceof IllegalArgumentException) throw (IllegalArgumentException)e;
                throw new SQLException("Cannot save user " + normalized, e);
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public boolean delete(String login) throws SQLException {
        try (Connection c = database.openConnection();
             PreparedStatement d = c.prepareStatement("DELETE FROM app_user WHERE login=?")) {
            d.setString(1, normalizeLogin(login));
            return d.executeUpdate() > 0;
        }
    }

    public List<String> listLogins() throws SQLException {
        List<String> logins = new ArrayList<String>();
        try (Connection c = database.openConnection();
             PreparedStatement q = c.prepareStatement("SELECT login FROM app_user WHERE login NOT LIKE 'prueba/%' ORDER BY login");
             ResultSet r = q.executeQuery()) {
            while (r.next()) logins.add(r.getString(1));
        }
        return Collections.unmodifiableList(logins);
    }

    public List<String> listRecentLogins(long since) throws SQLException {
        List<String> logins = new ArrayList<String>();
        try (Connection c = database.openConnection();
             PreparedStatement q = c.prepareStatement(
                 "SELECT login FROM app_user WHERE login NOT LIKE 'prueba/%' AND updated_at>? ORDER BY updated_at DESC,login")) {
            q.setLong(1, since);
            try (ResultSet r = q.executeQuery()) { while (r.next()) logins.add(r.getString(1)); }
        }
        return Collections.unmodifiableList(logins);
    }

    public int count() throws SQLException {
        try (Connection c = database.openConnection();
             PreparedStatement q = c.prepareStatement("SELECT count(*) FROM app_user WHERE login NOT LIKE 'prueba/%'");
             ResultSet r = q.executeQuery()) { return r.next() ? r.getInt(1) : 0; }
    }

    /**
     * Moves imported legacy user documents into normalized user tables. Source
     * files are removed only after the SQLite transaction has committed.
     */
    public void migrateLegacy(SqlitePropertiesStore legacy, File root) throws IOException, SQLException {
        List<String> paths = new ArrayList<String>();
        for (String path : legacy.list(null, ".properties")) {
            if (path.matches("_[^/]+\\.properties") || path.matches("prueba/[^/]+\\.properties")) paths.add(path);
        }
        try (Connection c = database.openConnection()) {
            c.setAutoCommit(false);
            try {
                boolean done;
                try (PreparedStatement q = c.prepareStatement("SELECT 1 FROM migration_marker WHERE name=?")) {
                    q.setString(1, "users_legacy_imported");
                    try (ResultSet r = q.executeQuery()) { done = r.next(); }
                }
                if (!done) {
                    for (String path : paths) migrateOne(c, path, legacy.load(path), legacy.lastModified(path));
                    try (PreparedStatement i = c.prepareStatement(
                            "INSERT INTO migration_marker(name,completed_at) VALUES(?,?)")) {
                        i.setString(1, "users_legacy_imported"); i.setLong(2, System.currentTimeMillis()); i.executeUpdate();
                    }
                }
                for (String path : paths) {
                    try (PreparedStatement d = c.prepareStatement("DELETE FROM legacy_properties_file WHERE path=?")) {
                        d.setString(1, path); d.executeUpdate();
                    }
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                if (e instanceof IOException) throw (IOException)e;
                if (e instanceof SQLException) throw (SQLException)e;
                throw new SQLException("User migration failed", e);
            } finally { c.setAutoCommit(true); }
        }
        for (String path : paths) {
            File source = new File(root, path.replace('/', File.separatorChar));
            if (source.exists() && !source.delete()) throw new IOException("Cannot remove migrated user file: " + source);
        }
    }

    private void migrateOne(Connection c, String path, Properties p, long updatedAt) throws SQLException {
        if (p == null || p.getProperty("usuario") == null)
            throw new SQLException("Invalid legacy user document: " + path);
        String login = path.startsWith("prueba/") ? path.substring("prueba/".length(), path.length() - ".properties".length())
                : path.substring(1, path.length() - ".properties".length());
        login = normalizeLogin(path.startsWith("prueba/") ? "prueba/" + login : login);
        Long deletion = null;
        String deletionValue = p.getProperty("account_deletion_requested_at");
        if (deletionValue != null) try { deletion = Long.valueOf(deletionValue); } catch (NumberFormatException e) {
            throw new SQLException("Invalid account deletion timestamp for " + login, e);
        }
        long id;
        try (PreparedStatement i = c.prepareStatement(
                "INSERT INTO app_user(login,serialized_data,notes,account_deletion_requested_at,updated_at) VALUES(?,?,?,?,?) " +
                "ON CONFLICT(login) DO UPDATE SET serialized_data=excluded.serialized_data,notes=excluded.notes," +
                "account_deletion_requested_at=excluded.account_deletion_requested_at,updated_at=excluded.updated_at")) {
            i.setString(1, login); i.setString(2, p.getProperty("usuario")); i.setString(3, p.getProperty("notas", ""));
            if (deletion == null) i.setNull(4, java.sql.Types.BIGINT); else i.setLong(4, deletion.longValue());
            i.setLong(5, updatedAt); i.executeUpdate();
        }
        try (PreparedStatement q = c.prepareStatement("SELECT user_id FROM app_user WHERE login=?")) {
            q.setString(1, login); try (ResultSet r = q.executeQuery()) { r.next(); id = r.getLong(1); }
        }
        try (PreparedStatement t = c.prepareStatement("INSERT INTO user_training(user_id,round_no,serialized_data) VALUES(?,?,?)");
             PreparedStatement e = c.prepareStatement("INSERT INTO user_property_extension(user_id,property_key,property_value) VALUES(?,?,?)")) {
            for (String key : p.stringPropertyNames()) {
                if (key.matches("entrenamiento[0-9]+")) {
                    t.setLong(1, id); t.setInt(2, Integer.parseInt(key.substring("entrenamiento".length())));
                    t.setString(3, p.getProperty(key)); t.addBatch();
                } else if (!"usuario".equals(key) && !"notas".equals(key) && !ACCOUNT_DELETION_KEY.equals(key)) {
                    e.setLong(1, id); e.setString(2, key); e.setString(3, p.getProperty(key)); e.addBatch();
                }
            }
            t.executeBatch(); e.executeBatch();
        }
    }

    private static final String ACCOUNT_DELETION_KEY = "account_deletion_requested_at";

    private static String normalizeLogin(String login) {
        if (login == null || login.trim().isEmpty()) throw new IllegalArgumentException("login is required");
        String value = login.toLowerCase(java.util.Locale.ROOT);
        if (value.indexOf('/') >= 0 && !value.startsWith("prueba/"))
            throw new IllegalArgumentException("Invalid login: " + login);
        if (value.startsWith("prueba/") && value.length() == "prueba/".length())
            throw new IllegalArgumentException("Invalid login: " + login);
        return value;
    }
}
