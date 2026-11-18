package com.formulamanager.sokker.persistence;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 * Owns the two independent persistence domains used by the application.
 * The Asistente and FactorX stores must never share a database.
 */
public final class SqlitePersistence {
    private static volatile SqlitePropertiesStore asistente;
    private static volatile File factorxDatabaseFile;
    private static volatile File factorxRoot;

    private SqlitePersistence() {}

    public static synchronized void initialize(File asistenteDirectory, File factorxDirectory)
            throws IOException, SQLException {
        if (asistenteDirectory == null || factorxDirectory == null) {
            throw new IllegalArgumentException("Both data directories are required");
        }
        File asistenteRoot = asistenteDirectory.getCanonicalFile();
        File factorxRoot = factorxDirectory.getCanonicalFile();
        if (asistenteRoot.equals(factorxRoot)
                || asistenteRoot.toPath().startsWith(factorxRoot.toPath())
                || factorxRoot.toPath().startsWith(asistenteRoot.toPath())) {
            throw new IOException("Asistente and FactorX data directories must be independent");
        }

        ensureDirectory(asistenteRoot);
        ensureDirectory(factorxRoot);
        SqliteDatabase asistenteDatabase = new SqliteDatabase(new File(asistenteRoot, "asistente.db"));
        SqliteDatabase factorxDatabase = new SqliteDatabase(new File(factorxRoot, "factorx.db"), false);

        // Import both domains before publishing either store. The importer is
        // idempotent and does not modify or delete the legacy source files.
        new LegacyPropertiesImporter(asistenteDatabase, asistenteRoot).importAll();
        new LegacyPropertiesImporter(factorxDatabase, factorxRoot).importAll();

        SqlitePropertiesStore newAsistente = new SqlitePropertiesStore(asistenteDatabase, asistenteRoot);
        new TeamPlayerDAO(asistenteDatabase).migrateLegacy(newAsistente, asistenteRoot);
        new UserDAO(asistenteDatabase).migrateLegacy(newAsistente, asistenteRoot);
        SqlitePropertiesStore factorxMigrationSource = new SqlitePropertiesStore(factorxDatabase, factorxRoot);
        new FactorxDAO(factorxDatabase).migrateLegacy(factorxMigrationSource, factorxRoot);
        asistente = newAsistente;
        SqlitePersistence.factorxRoot = factorxRoot;
        SqlitePersistence.factorxDatabaseFile = factorxDatabase.getDatabaseFile();
    }

    public static SqlitePropertiesStore forFile(File file) throws IOException {
        if (file == null) return null;
        File canonical = file.getCanonicalFile();
        File currentFactorxRoot = factorxRoot;
        if (currentFactorxRoot != null && canonical.toPath().startsWith(currentFactorxRoot.toPath())) {
            throw new IllegalStateException("Factor X data must be accessed through FactorxDAO: " + file);
        }
        SqlitePropertiesStore currentAsistente = asistente;
        if (currentAsistente != null && canonical.toPath().startsWith(currentAsistente.getRoot().toPath())) {
            String relative = currentAsistente.relativePath(canonical);
            if (TeamPlayerDAO.isManagedPath(relative)) {
                throw new IllegalStateException("Team player data must be accessed through TeamPlayerDAO: " + file);
            }
            return currentAsistente;
        }
        return null;
    }

    private static void ensureDirectory(File directory) throws IOException {
        if (!directory.isDirectory() && !directory.mkdirs() && !directory.isDirectory()) {
            throw new IOException("Cannot create data directory: " + directory);
        }
    }

    public static Properties load(File file) throws IOException {
        SqlitePropertiesStore store = forFile(file);
        if (store == null) return null;
        try {
            return store.load(store.relativePath(file));
        } catch (SQLException e) {
            throw new IOException("Cannot read SQLite properties: " + file, e);
        }
    }

    public static boolean exists(File file) throws IOException {
        SqlitePropertiesStore store = forFile(file);
        if (store == null) return file != null && file.isFile();
        try {
            return store.exists(store.relativePath(file));
        } catch (SQLException e) {
            throw new IOException("Cannot check SQLite properties: " + file, e);
        }
    }

    public static void save(File file, Properties properties) throws IOException {
        SqlitePropertiesStore store = forFile(file);
        if (store == null) throw new IOException("No SQLite store configured for " + file);
        try {
            store.save(store.relativePath(file), properties);
        } catch (SQLException e) {
            throw new IOException("Cannot save SQLite properties: " + file, e);
        }
    }

    public static boolean delete(File file) throws IOException {
        SqlitePropertiesStore store = forFile(file);
        if (store == null) return file != null && file.delete();
        try {
            return store.delete(store.relativePath(file));
        } catch (SQLException e) {
            throw new IOException("Cannot delete SQLite properties: " + file, e);
        }
    }

    public static List<String> list(File root, String prefix, String suffix) throws IOException {
        SqlitePropertiesStore store = forFile(root);
        if (store == null) return null;
        try {
            return store.list(prefix, suffix);
        } catch (SQLException e) {
            throw new IOException("Cannot list SQLite properties below " + root, e);
        }
    }

    public static long lastModified(File file) throws IOException {
        SqlitePropertiesStore store = forFile(file);
        if (store == null) return file == null ? 0L : file.lastModified();
        try {
            return store.lastModified(store.relativePath(file));
        } catch (SQLException e) {
            throw new IOException("Cannot read SQLite properties timestamp: " + file, e);
        }
    }

    public static boolean isManagedPlayerFile(File file) throws IOException {
        SqlitePropertiesStore current = asistente;
        return current != null && file != null
                && TeamPlayerDAO.isManagedPath(current.relativePath(file));
    }

    public static boolean isInitialized() {
        return asistente != null && factorxDatabaseFile != null;
    }

    public static File getAsistenteDatabaseFile() {
        return asistente == null ? null : new File(asistente.getRoot(), "asistente.db");
    }

    public static File getFactorxDatabaseFile() {
        return factorxDatabaseFile;
    }

    public static synchronized void shutdown() {
        asistente = null;
        factorxDatabaseFile = null;
        factorxRoot = null;
    }
}
