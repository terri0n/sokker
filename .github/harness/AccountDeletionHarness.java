package com.formulamanager.sokker.bo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Properties;

import com.formulamanager.sokker.auxiliares.SystemUtil;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.persistence.SqlitePersistence;
import com.formulamanager.sokker.persistence.SqliteDatabase;
import com.formulamanager.sokker.entity.Usuario;

public final class AccountDeletionHarness {
    private AccountDeletionHarness() {}

    public static void main(String[] args) throws Exception {
        uniqueUserDeletesOwnedData();
        sharedTidPreservesTeamData();
        nationalTeamReferencePreservesTeamData();
        nationalTeamDataIsPreserved();
        configuredAdminCannotBeDeleted();
        deletionIsIdempotent();
        accountFileSurvivesInjectedFailure();
    }

    private static void uniqueUserDeletesOwnedData() throws Exception {
        Path data = configure("sokker-delete-unique-", "admin");
        seedUser(data, "alice", 123, null, true);
        seedTeamFiles(data, 123);
        touch(data.resolve("backup/999_1199.properties"));
        touch(data.getParent().resolve("asistente-logs/_alice.log"));
        HashMap<String, String> ntdb = new HashMap<String, String>();
        ntdb.put("alice", "bob,charlie");
        ntdb.put("owner", "alice,bob");
        ntdb.put("owner2", "malice,ALICE,charlie");
        Util.guardar_hashmap(ntdb, "NTDB");
        initializeSqlite(data);

        AccountDeletionService.DeletionManifest manifest = AccountDeletionService.buildManifest("alice");
        require(manifest.isDeleteTeamData(), "Unique TID was not classified as exclusive");
        require(Integer.valueOf(123).equals(manifest.getTid()), "Manifest used the wrong TID");
        AccountDeletionService.execute(manifest);

        require(UsuarioBO.leer_usuario("alice", false) == null, "Account row still exists in SQLite");
        require(!Files.exists(data.getParent().resolve("asistente-logs/_alice.log")), "Personal log still exists");
        require(!hasTeamData(123), "Team-owned SQLite data still exists");
        require(!hasTestUser(123), "Team-scoped test account still exists");
        require(!Files.exists(data.resolve("backup/123_1199.properties")), "Team backup still exists");
        require(!Files.exists(data.resolve("backup/123_1199_historico.properties")), "Historical team backup still exists");
        require(hasTeamData(999), "Unrelated team backup data was deleted");

        HashMap<String, String> saved = Util.leer_hashmap("NTDB");
        require(!saved.containsKey("alice"), "Deleted user's NTDB entry remains");
        require("bob".equals(saved.get("owner")), "Scout list was not cleaned exactly");
        require("malice,charlie".equals(saved.get("owner2")), "Scout cleanup removed a partial login or missed case-insensitive exact token");
    }

    private static void sharedTidPreservesTeamData() throws Exception {
        Path data = configure("sokker-delete-shared-", "admin");
        seedUser(data, "alice", 123, null, true);
        seedUser(data, "other", 123, null, false);
        seedTeamFiles(data, 123);
        touch(data.resolve("logs/_alice.log"));
        initializeSqlite(data);

        AccountDeletionService.DeletionManifest manifest = AccountDeletionService.buildManifest("alice");
        require(!manifest.isDeleteTeamData(), "Shared TID was treated as exclusive");
        AccountDeletionService.execute(manifest);

        require(UsuarioBO.leer_usuario("alice", false) == null, "Deleted account remains in SQLite");
        require(UsuarioBO.leer_usuario("other", false) != null, "Other account was deleted");
        require(hasTeamData(123), "Shared team data or backup was deleted");
        require(hasTestUser(123), "Shared team-scoped test account was deleted");
    }

    private static void nationalTeamReferencePreservesTeamData() throws Exception {
        Path data = configure("sokker-delete-shared-nt-", "admin");
        seedUser(data, "alice", 123, null, true);
        seedUser(data, "scout", 777, 123, false);
        seedTeamFiles(data, 123);
        initializeSqlite(data);

        AccountDeletionService.DeletionManifest manifest = AccountDeletionService.buildManifest("alice");
        require(!manifest.isDeleteTeamData(),
                "A team referenced as another account's national team was treated as exclusive");
        AccountDeletionService.execute(manifest);
        require(hasTeamData(123), "Team data referenced through tid_nt was deleted");
        require(hasTestUser(123), "Team-scoped test account referenced through tid_nt was deleted");
    }

    private static void nationalTeamDataIsPreserved() throws Exception {
        Path data = configure("sokker-delete-nt-", "admin");
        seedUser(data, "alice", 123, 34, true);
        seedTeamFiles(data, 123);
        touch(data.resolve("34.properties"));
        touch(data.resolve("34_historico.properties"));
        initializeSqlite(data);

        AccountDeletionService.execute(AccountDeletionService.buildManifest("alice"));

        require(hasTeamData(34), "National-team data was deleted");
        require(!hasTestUser(123), "Deleted team's test account was retained");
    }

    private static void configuredAdminCannotBeDeleted() throws Exception {
        Path data = configure("sokker-delete-admin-", "admin");
        seedUser(data, "admin", 777, null, true);
        initializeSqlite(data);
        boolean failed = false;
        try {
            AccountDeletionService.buildManifest("admin");
        } catch (IOException expected) {
            failed = true;
        }
        require(failed, "Configured admin account was deletable");
        require(UsuarioBO.leer_usuario("admin", false) != null, "Admin account changed during manifest build");
    }

    private static void deletionIsIdempotent() throws Exception {
        Path data = configure("sokker-delete-idempotent-", "admin");
        seedUser(data, "alice", 123, null, true);
        seedTeamFiles(data, 123);
        initializeSqlite(data);
        AccountDeletionService.DeletionManifest manifest = AccountDeletionService.buildManifest("alice");
        AccountDeletionService.execute(manifest);
        AccountDeletionService.execute(manifest);
        require(UsuarioBO.leer_usuario("alice", false) == null, "Second execution recreated or retained the account");
    }

    private static void accountFileSurvivesInjectedFailure() throws Exception {
        Path data = configure("sokker-delete-failure-", "admin");
        seedUser(data, "alice", 123, null, true);
        seedTeamFiles(data, 123);
        initializeSqlite(data);
        final AccountDeletionService.DeletionManifest manifest = AccountDeletionService.buildManifest("alice");
        boolean failed = false;
        try {
            AccountDeletionService.execute(manifest, new AccountDeletionService.FileOperations() {
                @Override
                public void deleteIfExists(File file) throws IOException {
                    Files.deleteIfExists(file.toPath());
                }
                @Override
                public void deleteTeamData(int tid) throws IOException {
                    throw new IOException("injected deletion failure");
                }
            });
        } catch (IOException expected) {
            failed = true;
        }
        require(failed, "Injected deletion failure did not propagate");
        require(UsuarioBO.obtener_fecha_solicitud_borrado("alice") != null,
                "Pending deletion marker disappeared after a prior failure");
        require(hasTestUser(123), "Test account was deleted despite team-data deletion failure");
    }

    private static void initializeSqlite(Path data) throws Exception {
        Path factorx = Files.createTempDirectory("sokker-delete-factorx-");
        SqlitePersistence.initialize(data.toFile(), factorx.toFile());
    }

    private static boolean hasTestUser(int tid) {
        return UsuarioBO.leer_usuario("prueba/" + tid, false) != null;
    }

    private static boolean hasTeamData(int tid) throws Exception {
        try (java.sql.Connection connection = new SqliteDatabase(SqlitePersistence.getAsistenteDatabaseFile(), true).openConnection();
             java.sql.PreparedStatement query = connection.prepareStatement(
                 "SELECT (SELECT COUNT(*) FROM team_player WHERE tid=?) + " +
                 "(SELECT COUNT(*) FROM team_player_extension WHERE tid=?)")) {
            query.setInt(1, tid);
            query.setInt(2, tid);
            try (java.sql.ResultSet result = query.executeQuery()) {
                return result.next() && result.getInt(1) > 0;
            }
        }
    }

    private static Path configure(String prefix, String adminLogin) throws Exception {
        Path tomcat = Files.createTempDirectory(prefix);
        Path webapp = tomcat.resolve("webapps/sokker");
        Path data = tomcat.resolve("data");
        Files.createDirectories(webapp);
        Files.createDirectories(data);
        Files.createDirectories(data.resolve("backup"));
        Files.createDirectories(data.getParent().resolve("asistente-logs"));
        Files.createDirectories(data.resolve("prueba"));
        Files.createDirectories(tomcat.resolve("conf"));
        String dataPath = data.toAbsolutePath().toString() + File.separator;
        String serverXml = "<Server><GlobalNamingResources>"
                + "<Environment name=\"path\" value=\"" + xml(dataPath) + "\" type=\"java.lang.String\"/>"
                + "<Environment name=\"login\" value=\"" + xml(adminLogin) + "\" type=\"java.lang.String\"/>"
                + "</GlobalNamingResources></Server>";
        Files.write(tomcat.resolve("conf/server.xml"), serverXml.getBytes(StandardCharsets.UTF_8));
        SystemUtil.REAL_PATH = webapp.toAbsolutePath().toString() + File.separator;
        return data;
    }

    private static void seedUser(Path data, String login, int tid, Integer tidNt, boolean pending) throws Exception {
        Properties prop = new Properties();
        prop.setProperty("usuario", login + ",pw,sokker-" + login + "," + tid + ","
                + (tidNt == null ? "" : tidNt.toString()) + "," + tid + ",Team " + tid + ",,1200,,0,*");
        prop.setProperty("notas", "");
        if (pending) prop.setProperty(UsuarioBO.ACCOUNT_DELETION_REQUESTED_AT, "1727186400000");
        try (FileOutputStream output = new FileOutputStream(data.resolve("_" + login + ".properties").toFile())) {
            prop.store(output, null);
        }
    }

    private static void seedTeamFiles(Path data, int tid) throws Exception {
        touch(data.resolve(tid + ".properties"));
        touch(data.resolve(tid + "_historico.properties"));
        touch(data.resolve(tid + "_juveniles.properties"));
        touch(data.resolve(tid + "_juveniles_historico.properties"));
        Properties testProperties = new Properties();
        Usuario testUser = new Usuario("prueba/" + tid, new Usuario(Integer.valueOf(tid), "Team " + tid));
        testProperties.setProperty("usuario", testUser.serializar());
        File testAccount = data.resolve("prueba/" + tid + ".properties").toFile();
        Files.createDirectories(testAccount.toPath().getParent());
        try (FileOutputStream output = new FileOutputStream(testAccount)) {
            testProperties.store(output, null);
        }
        touch(data.resolve("backup/" + tid + "_1199.properties"));
        touch(data.resolve("backup/" + tid + "_1199_historico.properties"));
    }

    private static void touch(Path path) throws Exception {
        Files.createDirectories(path.getParent());
        Files.write(path, "fixture".getBytes(StandardCharsets.UTF_8));
    }

    private static Properties load(File file) throws Exception {
        Properties prop = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            prop.load(input);
        }
        return prop;
    }

    private static String xml(String value) {
        return value.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
