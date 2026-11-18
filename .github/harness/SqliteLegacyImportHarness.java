import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DriverManager;
import java.util.Properties;

import com.formulamanager.sokker.persistence.LegacyPropertiesImporter;
import com.formulamanager.sokker.persistence.SqliteDatabase;
import com.formulamanager.sokker.persistence.SqlitePropertiesStore;

/** Regression harness for lossless, repeatable legacy .properties snapshot import. */
public class SqliteLegacyImportHarness {
    public static void main(String[] args) throws Exception {
        File root = Files.createTempDirectory("sa-legacy-import").toFile();
        File source = new File(root, "legacy");
        File nested = new File(source, "factorx");
        if (!nested.mkdirs()) throw new IllegalStateException("Cannot create fixture directory");
        byte[] original = ("usuario=alice,secret,\n" +
                "future_field=must survive\n" +
                "entrenamiento1200=Porteria-Defensa---,\n" +
                "notas=áéíóú\n").getBytes(StandardCharsets.ISO_8859_1);
        File fixture = new File(source, "_alice.properties");
        Files.write(fixture.toPath(), original);
        Files.write(new File(nested, "config.properties").toPath(),
                "0_pid=123\n0_future_skill=17\n".getBytes(StandardCharsets.ISO_8859_1));

        File dbFile = new File(root, "db/sokker.db");
        SqliteDatabase database = new SqliteDatabase(dbFile);
        LegacyPropertiesImporter importer = new LegacyPropertiesImporter(database, source);
        LegacyPropertiesImporter.ImportReport first = importer.importAll();
        require(first.getDiscovered() == 2, "Expected both root and nested properties files");
        require(first.getImported() == 2, "Expected two files imported");
        require("alice,secret,".equals(readProperty(database, "_alice.properties", "usuario")),
                "Known property was not preserved");
        require("must survive".equals(readProperty(database, "_alice.properties", "future_field")),
                "Unknown property was not preserved");
        require("áéíóú".equals(readProperty(database, "_alice.properties", "notas")),
                "ISO-8859-1 property value was not preserved");
        require("17".equals(readProperty(database, "factorx/config.properties", "0_future_skill")),
                "Nested FactorX property was not preserved");
        require(!hasColumn(database, "legacy_properties_file", "content"),
                "Original file BLOB duplicates the parsed key/value data");
        require("delete".equals(readJournalMode(database)),
                "Database must use DELETE journaling to avoid persistent sidecar files");
        require(!new File(dbFile.getAbsolutePath() + "-wal").exists(),
                "WAL sidecar must not be created");
        require(!new File(dbFile.getAbsolutePath() + "-shm").exists(),
                "SHM sidecar must not be created");
        require(!new File(dbFile.getAbsolutePath() + "-journal").exists(),
                "SQLite rollback journal must be removed after commit");

        SqlitePropertiesStore store = new SqlitePropertiesStore(database, source);
        Properties loaded = store.load("_alice.properties");
        require(loaded != null && "must survive".equals(loaded.getProperty("future_field")),
                "Properties compatibility store lost an unknown key");
        loaded.setProperty("notas", "actualizado");
        store.save("_alice.properties", loaded);
        Properties roundTrip = store.load("_alice.properties");
        require("must survive".equals(roundTrip.getProperty("future_field")),
                "Properties round-trip lost an unknown key");
        require("actualizado".equals(roundTrip.getProperty("notas")),
                "Properties round-trip did not save the changed value");
        require(store.exists("_alice.properties"), "Saved properties document is not discoverable");
        require(store.list("_", ".properties").size() == 1,
                "SQLite path listing did not apply exact prefix/suffix filters");

        LegacyPropertiesImporter.ImportReport second = importer.importAll();
        require(second.getImported() == 0 && second.getUnchanged() == 2,
                "Repeated import must be idempotent");
        require("actualizado".equals(store.load("_alice.properties").getProperty("notas")),
                "A later import overwrote data already modified in SQLite");
        require(java.util.Arrays.equals(original, Files.readAllBytes(fixture.toPath())),
                "Source file was modified");

        File external = new File(root, "outside/iberica.properties");
        if (!external.getParentFile().mkdirs()) throw new IllegalStateException("Cannot create external fixture");
        byte[] externalBytes = "competicion_num_grupos=2\nfuture_iberia_key=preserve\n"
                .getBytes(StandardCharsets.ISO_8859_1);
        Files.write(external.toPath(), externalBytes);
        importer.importExternal(external, "iberica.properties");
        require("2".equals(readProperty(database, "iberica.properties", "competicion_num_grupos")),
                "External legacy properties were not imported");
        require("preserve".equals(readProperty(database, "iberica.properties", "future_iberia_key")),
                "Unknown external properties key was not preserved");
        require(external.isFile(), "External source was deleted before migration completion");
        boolean changedSourceRejected = false;
        Files.write(external.toPath(), "competicion_num_grupos=3\n".getBytes(StandardCharsets.ISO_8859_1));
        try {
            importer.importExternal(external, "iberica.properties");
        } catch (java.sql.SQLException expected) {
            changedSourceRejected = true;
        }
        require(changedSourceRejected, "Changed external source overwrote an existing SQLite document");
        require("2".equals(readProperty(database, "iberica.properties", "competicion_num_grupos")),
                "Rejected external import changed SQLite data");
        Files.write(external.toPath(), externalBytes);
        importer.retireExternal(external, "iberica.properties");
        require(!external.exists(), "Verified external source was not retired");

        verifyVersionOneMigration(root);
        System.out.println("SqliteLegacyImportHarness: OK");
    }

    private static void verifyVersionOneMigration(File root) throws Exception {
        File oldFile = new File(root, "v1/old.db");
        if (!oldFile.getParentFile().mkdirs()) throw new IllegalStateException("Cannot create v1 fixture");
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + oldFile.getAbsolutePath());
             Statement statement = c.createStatement()) {
            statement.execute("PRAGMA foreign_keys=ON");
            statement.execute("CREATE TABLE schema_version(version INTEGER NOT NULL)");
            statement.execute("INSERT INTO schema_version(version) VALUES(1)");
            statement.execute("CREATE TABLE legacy_properties_file(path TEXT PRIMARY KEY NOT NULL, sha256 TEXT NOT NULL, content BLOB NOT NULL, imported_at INTEGER NOT NULL)");
            statement.execute("CREATE TABLE legacy_properties_entry(path TEXT NOT NULL REFERENCES legacy_properties_file(path) ON DELETE CASCADE, property_key TEXT NOT NULL, property_value TEXT NOT NULL, PRIMARY KEY(path,property_key))");
            statement.execute("INSERT INTO legacy_properties_file(path,sha256,content,imported_at) VALUES('old.properties','hash',X'61',123)");
            statement.execute("INSERT INTO legacy_properties_entry(path,property_key,property_value) VALUES('old.properties','future.key','keep-me')");
        }
        SqliteDatabase upgraded = new SqliteDatabase(oldFile);
        upgraded.initialize();
        require("keep-me".equals(readProperty(upgraded, "old.properties", "future.key")),
                "Schema migration dropped existing key/value data");
        require(!hasColumn(upgraded, "legacy_properties_file", "content"),
                "Schema migration did not remove duplicate legacy BLOB");
        try (Connection c = upgraded.openConnection();
             Statement statement = c.createStatement();
             ResultSet result = statement.executeQuery("SELECT version FROM schema_version")) {
            require(result.next() && result.getInt(1) == 2, "Schema version was not upgraded to 2");
        }
    }

    private static String readProperty(SqliteDatabase database, String path, String key) throws Exception {
        try (Connection c = database.openConnection();
             PreparedStatement s = c.prepareStatement(
                     "SELECT property_value FROM legacy_properties_entry WHERE path=? AND property_key=?")) {
            s.setString(1, path);
            s.setString(2, key);
            try (ResultSet r = s.executeQuery()) {
                return r.next() ? r.getString(1) : null;
            }
        }
    }

    private static boolean hasColumn(SqliteDatabase database, String table, String column) throws Exception {
        try (Connection c = database.openConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (r.next()) if (column.equalsIgnoreCase(r.getString("name"))) return true;
            return false;
        }
    }

    private static String readJournalMode(SqliteDatabase database) throws Exception {
        try (Connection c = database.openConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery("PRAGMA journal_mode")) {
            return r.next() ? r.getString(1) : null;
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
