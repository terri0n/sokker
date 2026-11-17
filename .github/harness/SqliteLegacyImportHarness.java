import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

        SqliteDatabase database = new SqliteDatabase(new File(root, "db/sokker.db"));
        LegacyPropertiesImporter importer = new LegacyPropertiesImporter(database, source);
        LegacyPropertiesImporter.ImportReport first = importer.importAll();
        require(first.getDiscovered() == 2, "Expected both root and nested properties files");
        require(first.getImported() == 2, "Expected two files imported");
        require(java.util.Arrays.equals(original, readOriginal(database, "_alice.properties")),
                "Original bytes were not preserved");
        require("must survive".equals(readProperty(database, "_alice.properties", "future_field")),
                "Unknown property was not indexed");

        LegacyPropertiesImporter.ImportReport second = importer.importAll();
        require(second.getImported() == 0 && second.getUnchanged() == 2,
                "Repeated import must be idempotent");
        require(java.util.Arrays.equals(original, Files.readAllBytes(fixture.toPath())),
                "Source file was modified");
        System.out.println("SqliteLegacyImportHarness: OK");
    }

    private static byte[] readOriginal(SqliteDatabase database, String path) throws Exception {
        try (Connection c = database.openConnection();
             PreparedStatement s = c.prepareStatement("SELECT content FROM legacy_properties_file WHERE path=?")) {
            s.setString(1, path);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) throw new AssertionError("Missing imported file " + path);
                return r.getBytes(1);
            }
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

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
