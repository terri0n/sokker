package com.formulamanager.sokker.persistence;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Properties-compatible persistence over a single SQLite database.
 *
 * Each legacy document is stored as key/value rows. The SHA-256 column records
 * the original imported source version and is intentionally immutable after
 * import, so startup can detect a changed source before retiring it.
 */
public final class SqlitePropertiesStore {
    private final SqliteDatabase database;
    private final File root;

    public SqlitePropertiesStore(SqliteDatabase database, File root) throws IOException, SQLException {
        if (database == null || root == null) {
            throw new IllegalArgumentException("database and root are required");
        }
        this.database = database;
        this.root = root.getCanonicalFile();
        database.initialize();
    }

    public File getRoot() {
        return root;
    }

    public Properties load(String relativePath) throws IOException, SQLException {
        String path = normalize(relativePath);
        try (Connection connection = database.openConnection()) {
            try (PreparedStatement exists = connection.prepareStatement(
                    "SELECT 1 FROM legacy_properties_file WHERE path=?")) {
                exists.setString(1, path);
                try (ResultSet result = exists.executeQuery()) {
                    if (!result.next()) return null;
                }
            }
            Properties properties = new Properties();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT property_key,property_value FROM legacy_properties_entry WHERE path=?")) {
                statement.setString(1, path);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        properties.setProperty(result.getString(1), result.getString(2));
                    }
                }
            }
            return properties;
        }
    }

    public void save(String relativePath, Properties properties) throws IOException, SQLException {
        if (properties == null) {
            throw new IllegalArgumentException("properties is required");
        }
        String path = normalize(relativePath);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        properties.store(output, null);
        byte[] bytes = output.toByteArray();
        String checksum = sha256(bytes);

        try (Connection connection = database.openConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO legacy_properties_file(path,sha256,imported_at) VALUES(?,?,?) " +
                        "ON CONFLICT(path) DO UPDATE SET imported_at=excluded.imported_at")) {
                    statement.setString(1, path);
                    statement.setString(2, checksum);
                    statement.setLong(3, System.currentTimeMillis());
                    statement.executeUpdate();
                }
                try (PreparedStatement delete = connection.prepareStatement(
                        "DELETE FROM legacy_properties_entry WHERE path=?")) {
                    delete.setString(1, path);
                    delete.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO legacy_properties_entry(path,property_key,property_value) VALUES(?,?,?)")) {
                    for (String key : properties.stringPropertyNames()) {
                        statement.setString(1, path);
                        statement.setString(2, key);
                        statement.setString(3, properties.getProperty(key));
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                if (e instanceof SQLException) throw (SQLException)e;
                throw new SQLException("Cannot save properties document " + path, e);
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public boolean exists(String relativePath) throws SQLException {
        String path = normalize(relativePath);
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM legacy_properties_file WHERE path=?")) {
            statement.setString(1, path);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public long lastModified(String relativePath) throws SQLException {
        String path = normalize(relativePath);
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT imported_at FROM legacy_properties_file WHERE path=?")) {
            statement.setString(1, path);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getLong(1) : 0L;
            }
        }
    }

    public boolean delete(String relativePath) throws SQLException {
        String path = normalize(relativePath);
        if (database.isPlayerGroupsEnabled() && path.matches("_[^/]+\\.properties")) {
            String login = path.substring(1, path.length() - ".properties".length());
            return deleteAccount(path, login);
        }
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM legacy_properties_file WHERE path=?")) {
            statement.setString(1, path);
            return statement.executeUpdate() > 0;
        }
    }

    private boolean deleteAccount(String path, String login) throws SQLException {
        try (Connection connection = database.openConnection()) {
            connection.setAutoCommit(false);
            try {
                boolean deleted;
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM legacy_properties_file WHERE path=?")) {
                    statement.setString(1, path);
                    deleted = statement.executeUpdate() > 0;
                }
                boolean userDeleted;
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM app_user WHERE login=?")) {
                    statement.setString(1, login);
                    userDeleted = statement.executeUpdate() > 0;
                }
                connection.commit();
                return deleted || userDeleted;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<String> list(String prefix, String suffix) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT path FROM legacy_properties_file WHERE 1=1");
        if (prefix != null) sql.append(" AND substr(path,1,length(?))=?");
        if (suffix != null) sql.append(" AND substr(path,-length(?))=?");
        sql.append(" ORDER BY path");
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            if (prefix != null) {
                statement.setString(index++, prefix);
                statement.setString(index++, prefix);
            }
            if (suffix != null) {
                statement.setString(index++, suffix);
                statement.setString(index, suffix);
            }
            List<String> paths = new ArrayList<String>();
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) paths.add(result.getString(1));
            }
            return Collections.unmodifiableList(paths);
        }
    }

    public String relativePath(File file) throws IOException {
        Path rootPath = root.toPath();
        Path filePath = file.getCanonicalFile().toPath();
        if (!filePath.startsWith(rootPath)) {
            throw new IOException("Path is outside SQLite data root: " + file);
        }
        return rootPath.relativize(filePath).toString().replace(File.separatorChar, '/');
    }

    private String normalize(String path) {
        if (path == null || path.trim().isEmpty()) throw new IllegalArgumentException("path is required");
        String normalized = path.replace('\\', '/');
        while (normalized.startsWith("/")) normalized = normalized.substring(1);
        if (normalized.contains("../") || normalized.equals("..")) {
            throw new IllegalArgumentException("Path traversal is not allowed: " + path);
        }
        return normalized;
    }

    private static String escapeLike(String value) {
        return value.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    private static String sha256(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder value = new StringBuilder(digest.length * 2);
            for (byte b : digest) value.append(String.format("%02x", b & 0xff));
            return value.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
