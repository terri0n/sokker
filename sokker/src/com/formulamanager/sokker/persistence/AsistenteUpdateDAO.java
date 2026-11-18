package com.formulamanager.sokker.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Commits the four team datasets and the account's jornada marker together.
 * An update can therefore never advance a user while only part of the
 * corresponding player/junior state has been persisted.
 */
public final class AsistenteUpdateDAO {
    private final SqliteDatabase database;
    private final TeamPlayerDAO players;
    private final UserDAO users;

    public AsistenteUpdateDAO(SqliteDatabase database) {
        if (database == null || !database.isPlayerGroupsEnabled())
            throw new IllegalArgumentException("Asistente database required");
        this.database = database;
        this.players = new TeamPlayerDAO(database);
        this.users = new UserDAO(database);
    }

    public void save(int tid,
            Map<Integer, String> currentPlayers,
            Map<Integer, String> historicalPlayers,
            Map<Integer, String> currentJuniors,
            Map<Integer, String> historicalJuniors,
            UserDAO.SaveData user) throws SQLException {
        if (tid <= 0) throw new IllegalArgumentException("tid must be positive");
        if (user == null) throw new IllegalArgumentException("user save data is required");
        try (Connection connection = database.openConnection()) {
            connection.setAutoCommit(false);
            try {
                players.saveWithinTransaction(connection, tid, "PLAYER", 0, currentPlayers);
                players.saveWithinTransaction(connection, tid, "PLAYER_HISTORY", 0, historicalPlayers);
                players.saveWithinTransaction(connection, tid, "JUVENILE", 0, currentJuniors);
                players.saveWithinTransaction(connection, tid, "JUVENILE_HISTORY", 0, historicalJuniors);
                users.saveWithinTransaction(connection, user);
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                if (e instanceof SQLException) throw (SQLException)e;
                if (e instanceof IllegalArgumentException) throw (IllegalArgumentException)e;
                throw new SQLException("Cannot commit Asistente update for tid " + tid, e);
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
