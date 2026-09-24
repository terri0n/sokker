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

public final class AccountDeletionHarness {
    private AccountDeletionHarness() {}

    public static void main(String[] args) throws Exception {
        uniqueUserDeletesOwnedData();
        sharedTidPreservesTeamData();
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
        touch(data.resolve("logs/_alice.log"));
        HashMap<String, String> ntdb = new HashMap<String, String>();
        ntdb.put("alice", "bob,charlie");
        ntdb.put("owner", "alice,bob");
        ntdb.put("owner2", "malice,ALICE,charlie");
        Util.guardar_hashmap(ntdb, "NTDB");

        AccountDeletionService.DeletionManifest manifest = AccountDeletionService.buildManifest("alice");
        require(manifest.isDeleteTeamData(), "Unique TID was not classified as exclusive");
        require(Integer.valueOf(123).equals(manifest.getTid()), "Manifest used the wrong TID");
        AccountDeletionService.execute(manifest);

        require(!Files.exists(data.resolve("_alice.properties")), "Account file still exists");
        require(!Files.exists(data.resolve("logs/_alice.log")), "Personal log still exists");
        require(!Files.exists(data.resolve("123.properties")), "Current team data still exists");
        require(!Files.exists(data.resolve("123_historico.properties")), "Historical team data still exists");
        require(!Files.exists(data.resolve("123_juveniles.properties")), "Junior data still exists");
        require(!Files.exists(data.resolve("123_juveniles_historico.properties")), "Historical junior data still exists");
        require(!Files.exists(data.resolve("prueba/123.properties")), "Test-user data still exists");
        require(!Files.exists(data.resolve("backup/123_1199.properties")), "Team backup still exists");
        require(!Files.exists(data.resolve("backup/123_1199_historico.properties")), "Historical team backup still exists");
        require(Files.exists(data.resolve("backup/999_1199.properties")), "Unrelated backup was deleted");

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

        AccountDeletionService.DeletionManifest manifest = AccountDeletionService.buildManifest("alice");
        require(!manifest.isDeleteTeamData(), "Shared TID was treated as exclusive");
        AccountDeletionService.execute(manifest);

        require(!Files.exists(data.resolve("_alice.properties")), "Deleted account remains");
        require(Files.exists(data.resolve("_other.properties")), "Other account was deleted");
        require(Files.exists(data.resolve("123.properties")), "Shared current team data was deleted");
        require(Files.exists(data.resolve("123_historico.properties")), "Shared historical data was deleted");
        require(Files.exists(data.resolve("123_juveniles.properties")), "Shared junior data was deleted");
        require(Files.exists(data.resolve("backup/123_1199.properties")), "Shared backup was deleted");
    }

    private static void nationalTeamDataIsPreserved() throws Exception {
        Path data = configure("sokker-delete-nt-", "admin");
        seedUser(data, "alice", 123, 34, true);
        seedTeamFiles(data, 123);
        touch(data.resolve("34.properties"));
        touch(data.resolve("34_historico.properties"));

        AccountDeletionService.execute(AccountDeletionService.buildManifest("alice"));

        require(Files.exists(data.resolve("34.properties")), "National-team current data was deleted");
        require(Files.exists(data.resolve("34_historico.properties")), "National-team historical data was deleted");
    }

    private static void configuredAdminCannotBeDeleted() throws Exception {
        Path data = configure("sokker-delete-admin-", "admin");
        seedUser(data, "admin", 777, null, true);
        boolean failed = false;
        try {
            AccountDeletionService.buildManifest("admin");
        } catch (IOException expected) {
            failed = true;
        }
        require(failed, "Configured admin account was deletable");
        require(Files.exists(data.resolve("_admin.properties")), "Admin account changed during manifest build");
    }

    private static void deletionIsIdempotent() throws Exception {
        Path data = configure("sokker-delete-idempotent-", "admin");
        seedUser(data, "alice", 123, null, true);
        seedTeamFiles(data, 123);
        AccountDeletionService.DeletionManifest manifest = AccountDeletionService.buildManifest("alice");
        AccountDeletionService.execute(manifest);
        AccountDeletionService.execute(manifest);
        require(!Files.exists(data.resolve("_alice.properties")), "Second execution recreated or retained the account");
    }

    private static void accountFileSurvivesInjectedFailure() throws Exception {
        Path data = configure("sokker-delete-failure-", "admin");
        seedUser(data, "alice", 123, null, true);
        seedTeamFiles(data, 123);
        final AccountDeletionService.DeletionManifest manifest = AccountDeletionService.buildManifest("alice");
        boolean failed = false;
        try {
            AccountDeletionService.execute(manifest, new AccountDeletionService.FileOperations() {
                @Override
                public void deleteIfExists(File file) throws IOException {
                    if ("123.properties".equals(file.getName())) {
                        throw new IOException("injected deletion failure");
                    }
                    Files.deleteIfExists(file.toPath());
                }
            });
        } catch (IOException expected) {
            failed = true;
        }
        require(failed, "Injected deletion failure did not propagate");
        require(Files.exists(data.resolve("_alice.properties")), "Account file disappeared after a prior deletion failure");
        Properties account = load(data.resolve("_alice.properties").toFile());
        require(account.getProperty(UsuarioBO.ACCOUNT_DELETION_REQUESTED_AT) != null,
                "Pending deletion marker disappeared after a prior failure");
    }

    private static Path configure(String prefix, String adminLogin) throws Exception {
        Path tomcat = Files.createTempDirectory(prefix);
        Path webapp = tomcat.resolve("webapps/sokker");
        Path data = tomcat.resolve("data");
        Files.createDirectories(webapp);
        Files.createDirectories(data);
        Files.createDirectories(data.resolve("backup"));
        Files.createDirectories(data.resolve("logs"));
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
        touch(data.resolve("prueba/" + tid + ".properties"));
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
