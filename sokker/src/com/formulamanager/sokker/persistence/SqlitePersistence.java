package com.formulamanager.sokker.persistence;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Owns the two independent persistence domains used by the application.
 * The Asistente and FactorX stores must never share a database.
 */
public final class SqlitePersistence {
    private static volatile SqlitePropertiesStore asistente;
    private static volatile SqlitePropertiesStore factorx;

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

        SqliteDatabase asistenteDatabase = new SqliteDatabase(new File(asistenteRoot, "asistente.db"));
        SqliteDatabase factorxDatabase = new SqliteDatabase(new File(factorxRoot, "factorx.db"));

        // Import both domains before publishing either store. The importer is
        // idempotent and does not modify or delete the legacy source files.
        new LegacyPropertiesImporter(asistenteDatabase, asistenteRoot).importAll();
        new LegacyPropertiesImporter(factorxDatabase, factorxRoot).importAll();

        SqlitePropertiesStore newAsistente = new SqlitePropertiesStore(asistenteDatabase, asistenteRoot);
        SqlitePropertiesStore newFactorx = new SqlitePropertiesStore(factorxDatabase, factorxRoot);
        asistente = newAsistente;
        factorx = newFactorx;
    }

    public static SqlitePropertiesStore forFile(File file) throws IOException {
        if (file == null) return null;
        File canonical = file.getCanonicalFile();
        SqlitePropertiesStore currentFactorx = factorx;
        if (currentFactorx != null && canonical.toPath().startsWith(currentFactorx.getRoot().toPath())) {
            return currentFactorx;
        }
        SqlitePropertiesStore currentAsistente = asistente;
        if (currentAsistente != null && canonical.toPath().startsWith(currentAsistente.getRoot().toPath())) {
            return currentAsistente;
        }
        return null;
    }

    public static boolean isInitialized() {
        return asistente != null && factorx != null;
    }

    public static File getAsistenteDatabaseFile() {
        return asistente == null ? null : new File(asistente.getRoot(), "asistente.db");
    }

    public static File getFactorxDatabaseFile() {
        return factorx == null ? null : new File(factorx.getRoot(), "factorx.db");
    }

    public static synchronized void shutdown() {
        asistente = null;
        factorx = null;
    }
}
