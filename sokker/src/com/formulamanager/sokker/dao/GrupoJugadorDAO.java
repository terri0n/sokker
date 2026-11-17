package com.formulamanager.sokker.dao;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.formulamanager.sokker.entity.GrupoJugador;
import com.formulamanager.sokker.persistence.SqliteDatabase;
import com.formulamanager.sokker.persistence.SqlitePersistence;

/**
 * Persistence operations for the shared default groups and user-owned groups.
 * A user assignment overrides the shared team assignment for that account only.
 */
public final class GrupoJugadorDAO {
    private GrupoJugadorDAO() {}

    private static SqliteDatabase database() throws IOException {
        File file = SqlitePersistence.getAsistenteDatabaseFile();
        if (file == null) throw new IllegalStateException("Asistente SQLite is not initialized");
        return new SqliteDatabase(file, true);
    }

    public static List<GrupoJugador> obtenerGrupos(String login) throws IOException, SQLException {
        if (login == null || login.trim().isEmpty()) throw new IllegalArgumentException("login is required");
        SqliteDatabase database = database();
        try (Connection connection = database.openConnection()) {
            int userId = obtenerOCrearUsuario(connection, login);
            List<GrupoJugador> groups = new ArrayList<GrupoJugador>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT group_id,user_id,code,name,sort_order FROM player_group " +
                    "WHERE user_id IS NULL OR user_id=? ORDER BY sort_order,group_id")) {
                statement.setInt(1, userId);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) groups.add(readGroup(result));
                }
            }
            return groups;
        }
    }

    public static GrupoJugador crearGrupo(String login, String name) throws IOException, SQLException {
        String cleanName = validateName(name);
        try (Connection connection = database().openConnection()) {
            connection.setAutoCommit(false);
            try {
                int userId = obtenerOCrearUsuario(connection, login);
                int sortOrder = 7;
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT COALESCE(MAX(sort_order),6)+1 FROM player_group WHERE user_id=?")) {
                    statement.setInt(1, userId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) sortOrder = result.getInt(1);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO player_group(user_id,code,name,sort_order) VALUES(?,NULL,?,?)")) {
                    statement.setInt(1, userId);
                    statement.setString(2, cleanName);
                    statement.setInt(3, sortOrder);
                    statement.executeUpdate();
                }
                GrupoJugador group = obtenerGrupoPorNombre(connection, userId, cleanName);
                connection.commit();
                return group;
            } catch (Exception e) {
                connection.rollback();
                if (e instanceof SQLException) throw (SQLException)e;
                if (e instanceof IOException) throw (IOException)e;
                throw new SQLException("Cannot create player group", e);
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public static void renombrarGrupo(String login, int groupId, String name) throws IOException, SQLException {
        String cleanName = validateName(name);
        try (Connection connection = database().openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE player_group SET name=? WHERE group_id=? AND user_id=(SELECT user_id FROM app_user WHERE login=?)")) {
            statement.setString(1, cleanName);
            statement.setInt(2, groupId);
            statement.setString(3, login);
            if (statement.executeUpdate() != 1) throw new IllegalArgumentException("Group does not belong to this user");
        }
    }

    public static void eliminarGrupo(String login, int groupId) throws IOException, SQLException {
        try (Connection connection = database().openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM player_group WHERE group_id=? AND user_id=(SELECT user_id FROM app_user WHERE login=?)")) {
            statement.setInt(1, groupId);
            statement.setString(2, login);
            if (statement.executeUpdate() != 1) throw new IllegalArgumentException("Custom group does not belong to this user");
        }
    }

    public static void asignarGrupo(String login, int tid, int pid, int groupId) throws IOException, SQLException {
        if (tid <= 0 || pid <= 0) throw new IllegalArgumentException("tid and pid must be positive");
        try (Connection connection = database().openConnection()) {
            connection.setAutoCommit(false);
            try {
                int userId = obtenerOCrearUsuario(connection, login);
                GrupoJugador group = obtenerGrupo(connection, groupId, userId);
                if (group == null) throw new IllegalArgumentException("Group does not belong to this user");
                if (group.isDefault()) {
                    try (PreparedStatement delete = connection.prepareStatement(
                            "DELETE FROM player_group_assignment WHERE user_id=? AND tid=? AND pid=?")) {
                        delete.setInt(1, userId);
                        delete.setInt(2, tid);
                        delete.setInt(3, pid);
                        delete.executeUpdate();
                    }
                    try (PreparedStatement delete = connection.prepareStatement(
                            "DELETE FROM player_group_assignment WHERE user_id IS NULL AND tid=? AND pid=?")) {
                        delete.setInt(1, tid);
                        delete.setInt(2, pid);
                        delete.executeUpdate();
                    }
                    insertAssignment(connection, null, tid, pid, groupId);
                } else {
                    try (PreparedStatement delete = connection.prepareStatement(
                            "DELETE FROM player_group_assignment WHERE user_id=? AND tid=? AND pid=?")) {
                        delete.setInt(1, userId);
                        delete.setInt(2, tid);
                        delete.setInt(3, pid);
                        delete.executeUpdate();
                    }
                    insertAssignment(connection, Integer.valueOf(userId), tid, pid, groupId);
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                if (e instanceof SQLException) throw (SQLException)e;
                if (e instanceof IOException) throw (IOException)e;
                if (e instanceof IllegalArgumentException) throw (IllegalArgumentException)e;
                throw new SQLException("Cannot assign player group", e);
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public static Map<Integer, GrupoJugador> obtenerAsignaciones(String login, int tid)
            throws IOException, SQLException {
        try (Connection connection = database().openConnection()) {
            Integer userId = login == null ? null : obtenerOCrearUsuario(connection, login);
            String sql = "SELECT a.pid,g.group_id,g.user_id,g.code,g.name,g.sort_order " +
                    "FROM player_group_assignment a JOIN player_group g ON g.group_id=a.group_id " +
                    "WHERE a.tid=? AND (a.user_id IS NULL" + (userId == null ? "" : " OR a.user_id=?") + ") " +
                    "ORDER BY a.pid, CASE WHEN a.user_id IS NULL THEN 1 ELSE 0 END";
            Map<Integer, GrupoJugador> groups = new LinkedHashMap<Integer, GrupoJugador>();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, tid);
                if (userId != null) statement.setInt(2, userId.intValue());
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        int pid = result.getInt(1);
                        if (!groups.containsKey(Integer.valueOf(pid))) {
                            int groupId = result.getInt(2);
                            int owner = result.getInt(3);
                            Integer ownerId = result.wasNull() ? null : Integer.valueOf(owner);
                            groups.put(Integer.valueOf(pid), new GrupoJugador(groupId, ownerId,
                                    result.getString(4), result.getString(5), result.getInt(6)));
                        }
                    }
                }
            }
            return groups;
        }
    }

    private static void insertAssignment(Connection connection, Integer userId, int tid, int pid, int groupId)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO player_group_assignment(user_id,tid,pid,group_id) VALUES(?,?,?,?)")) {
            if (userId == null) statement.setNull(1, java.sql.Types.INTEGER);
            else statement.setInt(1, userId.intValue());
            statement.setInt(2, tid);
            statement.setInt(3, pid);
            statement.setInt(4, groupId);
            statement.executeUpdate();
        }
    }

    private static int obtenerOCrearUsuario(Connection connection, String login) throws SQLException {
        if (login == null || login.trim().isEmpty()) throw new IllegalArgumentException("login is required");
        try (PreparedStatement insert = connection.prepareStatement("INSERT OR IGNORE INTO app_user(login) VALUES(?)")) {
            insert.setString(1, login.trim());
            insert.executeUpdate();
        }
        try (PreparedStatement query = connection.prepareStatement("SELECT user_id FROM app_user WHERE login=?")) {
            query.setString(1, login.trim());
            try (ResultSet result = query.executeQuery()) {
                if (!result.next()) throw new SQLException("Cannot resolve user id for " + login);
                return result.getInt(1);
            }
        }
    }

    private static GrupoJugador obtenerGrupo(Connection connection, int groupId, int userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT group_id,user_id,code,name,sort_order FROM player_group " +
                "WHERE group_id=? AND (user_id IS NULL OR user_id=?)")) {
            statement.setInt(1, groupId);
            statement.setInt(2, userId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? readGroup(result) : null;
            }
        }
    }

    private static GrupoJugador obtenerGrupoPorNombre(Connection connection, int userId, String name) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT group_id,user_id,code,name,sort_order FROM player_group WHERE user_id=? AND name=? COLLATE NOCASE")) {
            statement.setInt(1, userId);
            statement.setString(2, name);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new SQLException("Created group cannot be reloaded");
                return readGroup(result);
            }
        }
    }

    private static GrupoJugador readGroup(ResultSet result) throws SQLException {
        int owner = result.getInt(2);
        Integer userId = result.wasNull() ? null : Integer.valueOf(owner);
        return new GrupoJugador(result.getInt(1), userId, result.getString(3),
                result.getString(4), result.getInt(5));
    }

    private static String validateName(String name) {
        if (name == null) throw new IllegalArgumentException("Group name is required");
        String clean = name.trim();
        if (clean.isEmpty() || clean.length() > 50) {
            throw new IllegalArgumentException("Group name must contain between 1 and 50 characters");
        }
        return clean;
    }
}
