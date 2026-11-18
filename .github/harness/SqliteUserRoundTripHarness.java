import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.formulamanager.sokker.persistence.LegacyPropertiesImporter;
import com.formulamanager.sokker.persistence.SqliteDatabase;
import com.formulamanager.sokker.persistence.SqlitePropertiesStore;
import com.formulamanager.sokker.persistence.UserDAO;

/** Exercises the complete legacy-account migration and SQLite round trip. */
public final class SqliteUserRoundTripHarness {
    public static void main(String[] args) throws Exception {
        File root = Files.createTempDirectory("sa-users-").toFile();
        File databaseFile = new File(root, "asistente.db");
        File source = new File(root, "_levi.properties");
        Properties legacy = new Properties();
        legacy.setProperty("usuario", "levi,md5,sokker,123,456,123,Club,NT,88,77,0,false,EN,34,false,false,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,*");
        legacy.setProperty("notas", "nota original");
        legacy.setProperty("entrenamiento88", "Porteria-Defensa---,,coach,5.5,4.5,*");
        legacy.setProperty("account_deletion_requested_at", "1700000000000");
        legacy.setProperty("future_user_field", "keep-me");
        try (FileOutputStream out = new FileOutputStream(source)) { legacy.store(out, null); }

        SqliteDatabase database = new SqliteDatabase(databaseFile);
        new LegacyPropertiesImporter(database, root).importAll();
        SqlitePropertiesStore legacyStore = new SqlitePropertiesStore(database, root);
        UserDAO dao = new UserDAO(database);
        dao.migrateLegacy(legacyStore, root);

        if (source.exists()) throw new AssertionError("Legacy user file was not retired after commit");
        UserDAO.UserRecord record = dao.load("LEVI");
        if (record == null) throw new AssertionError("Migrated user missing");
        if (!"nota original".equals(record.getNotes())) throw new AssertionError("Notes were lost");
        if (!Long.valueOf(1700000000000L).equals(record.getDeletionRequestedAt()))
            throw new AssertionError("Deletion timestamp was lost");
        if (!"Porteria-Defensa---,,coach,5.5,4.5,*".equals(record.getTraining().get(88)))
            throw new AssertionError("Training history was lost");
        if (!"keep-me".equals(record.getExtensions().getProperty("future_user_field")))
            throw new AssertionError("Unknown user property was lost");

        Map<Integer, String> training = new HashMap<Integer, String>(record.getTraining());
        training.put(89, "Defensa,,,,,*");
        Properties extensions = record.getExtensions();
        extensions.setProperty("future_second_field", "also-kept");
        dao.save("levi", record.getSerialized(), "nota modificada", record.getDeletionRequestedAt(), training, extensions);

        UserDAO.UserRecord reloaded = dao.load("levi");
        if (!"nota modificada".equals(reloaded.getNotes())) throw new AssertionError("Updated notes were not saved");
        if (!"Defensa,,,,,*".equals(reloaded.getTraining().get(89))) throw new AssertionError("New training row missing");
        if (!"keep-me".equals(reloaded.getExtensions().getProperty("future_user_field"))
                || !"also-kept".equals(reloaded.getExtensions().getProperty("future_second_field")))
            throw new AssertionError("Unknown extension properties were not round-tripped");
        if (dao.count() != 1 || !dao.listLogins().contains("levi"))
            throw new AssertionError("User listing/count is inconsistent");
        if (!dao.delete("levi") || dao.load("levi") != null)
            throw new AssertionError("User deletion failed");
        delete(root);
    }

    private static void delete(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) for (File child : children) delete(child);
        }
        file.delete();
    }
}
