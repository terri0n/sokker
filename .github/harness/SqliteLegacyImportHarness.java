import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.formulamanager.sokker.persistence.LegacyPropertiesImporter;
import com.formulamanager.sokker.persistence.SqliteDatabase;

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

        LegacyPropertiesImporter.ImportReport second = importer.importAll();
        require(second.getImported() == 0 && second.getUnchanged() == 2,
                "Repeated import must be idempotent");
        require(java.util.Arrays.equals(original, Files.readAllBytes(fixture.toPath())),
                "Source file was modified");
        System.out.println("SqliteLegacyImportHarness: OK");
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
