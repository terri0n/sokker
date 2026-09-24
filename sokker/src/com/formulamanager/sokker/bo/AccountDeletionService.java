package com.formulamanager.sokker.bo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.formulamanager.sokker.auxiliares.SystemUtil;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.entity.Usuario;

/**
 * Borrado definitivo de una cuenta del Asistente.
 *
 * El reset de jugadores es un flujo distinto y no debe usar esta clase.
 */
public final class AccountDeletionService {
    private AccountDeletionService() {}

    interface FileOperations {
        void deleteIfExists(File file) throws IOException;
    }

    private static final FileOperations REAL_FILE_OPERATIONS = new FileOperations() {
        @Override
        public void deleteIfExists(File file) throws IOException {
            Files.deleteIfExists(file.toPath());
        }
    };

    public static final class DeletionManifest {
        private final String login;
        private final Integer tid;
        private final boolean deleteTeamData;
        private final List<File> teamFiles;
        private final File personalLog;
        private final File accountFile;
        private final List<String> scoutOwnersReferencingLogin;

        private DeletionManifest(String login, Integer tid, boolean deleteTeamData,
                List<File> teamFiles, File personalLog, File accountFile,
                List<String> scoutOwnersReferencingLogin) {
            this.login = login;
            this.tid = tid;
            this.deleteTeamData = deleteTeamData;
            this.teamFiles = Collections.unmodifiableList(new ArrayList<File>(teamFiles));
            this.personalLog = personalLog;
            this.accountFile = accountFile;
            this.scoutOwnersReferencingLogin = Collections.unmodifiableList(
                    new ArrayList<String>(scoutOwnersReferencingLogin));
        }

        public String getLogin() {
            return login;
        }

        public Integer getTid() {
            return tid;
        }

        public boolean isDeleteTeamData() {
            return deleteTeamData;
        }

        /**
         * Rutas que se eliminan antes del fichero de cuenta, en orden de ejecución.
         */
        public List<File> getFilesToDeleteBeforeAccount() {
            List<File> files = new ArrayList<File>(teamFiles);
            if (personalLog != null) {
                files.add(personalLog);
            }
            return Collections.unmodifiableList(files);
        }

        public File getAccountFile() {
            return accountFile;
        }

        public List<String> getScoutOwnersReferencingLogin() {
            return scoutOwnersReferencingLogin;
        }
    }

    public static DeletionManifest buildManifest(String login) throws IOException {
        if (login == null || login.trim().isEmpty()) {
            throw new IOException("Usuario no especificado");
        }

        Usuario target = UsuarioBO.leer_usuario(login, false);
        if (target == null) {
            throw new IOException("Usuario no encontrado: " + login);
        }

        String targetLogin = target.getLogin().toLowerCase();
        String adminLogin = SystemUtil.getVar(SystemUtil.LOGIN);
        if (adminLogin != null && adminLogin.equalsIgnoreCase(targetLogin)) {
            throw new IOException("La cuenta administradora no se puede borrar");
        }

        String basePath = SystemUtil.getVar(SystemUtil.PATH);
        Integer tid = target.getTid();
        boolean deleteTeamData = tid != null && !isTidReferencedByAnotherAccount(targetLogin, tid);

        List<File> teamFiles = new ArrayList<File>();
        if (deleteTeamData) {
            addIfExists(teamFiles, new File(basePath + tid + ".properties"));
            addIfExists(teamFiles, new File(basePath + tid + "_historico.properties"));
            addIfExists(teamFiles, new File(basePath + tid + "_juveniles.properties"));
            addIfExists(teamFiles, new File(basePath + tid + "_juveniles_historico.properties"));
            addIfExists(teamFiles, new File(basePath + "prueba/" + tid + ".properties"));
            addAllowedBackups(teamFiles, new File(basePath + "backup"), tid);
            sortByCanonicalPath(teamFiles);
        }

        File personalLog = new File(basePath + "logs/_" + targetLogin + ".log");
        if (!personalLog.isFile()) {
            personalLog = null;
        }

        File accountFile = new File(basePath + "_" + targetLogin + ".properties");
        List<String> scoutOwners = findScoutOwners(targetLogin);

        return new DeletionManifest(targetLogin, tid, deleteTeamData, teamFiles,
                personalLog, accountFile, scoutOwners);
    }

    private static boolean isTidReferencedByAnotherAccount(String targetLogin, Integer tid) {
        for (String archivo : UsuarioBO.obtener_usuarios()) {
            if (archivo == null || !archivo.startsWith("_") || !archivo.endsWith(".properties")) {
                continue;
            }
            String login = archivo.substring(1, archivo.length() - ".properties".length());
            if (login.equalsIgnoreCase(targetLogin)) {
                continue;
            }
            Usuario other = UsuarioBO.leer_usuario(login, false);
            if (other != null && tid.equals(other.getTid())) {
                return true;
            }
        }
        return false;
    }

    private static void addIfExists(List<File> files, File file) {
        if (file.isFile()) {
            files.add(file);
        }
    }

    private static void addAllowedBackups(List<File> files, File backupDir, Integer tid) {
        if (!backupDir.isDirectory()) {
            return;
        }
        final Pattern allowed = Pattern.compile("^" + Pattern.quote(String.valueOf(tid))
                + "_[0-9]+(?:_historico)?(?:_juveniles(?:_historico)?)?\\.properties$");
        File[] candidates = backupDir.listFiles();
        if (candidates == null) {
            return;
        }
        for (File candidate : candidates) {
            if (candidate.isFile() && allowed.matcher(candidate.getName()).matches()) {
                files.add(candidate);
            }
        }
    }

    private static void sortByCanonicalPath(List<File> files) throws IOException {
        final HashMap<File, String> canonical = new HashMap<File, String>();
        for (File file : files) {
            canonical.put(file, file.getCanonicalPath());
        }
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                return canonical.get(a).compareTo(canonical.get(b));
            }
        });
    }

    private static List<String> findScoutOwners(String login) throws IOException {
        HashMap<String, String> map = Util.leer_hashmap("NTDB");
        List<String> owners = new ArrayList<String>();
        for (Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(login)) {
                continue;
            }
            if (containsExactLogin(entry.getValue(), login)) {
                owners.add(entry.getKey());
            }
        }
        Collections.sort(owners, String.CASE_INSENSITIVE_ORDER);
        return owners;
    }

    private static boolean containsExactLogin(String csv, String login) {
        if (csv == null || csv.trim().isEmpty()) {
            return false;
        }
        for (String token : csv.split(",")) {
            if (token.trim().equalsIgnoreCase(login)) {
                return true;
            }
        }
        return false;
    }

    public static void execute(DeletionManifest manifest) throws IOException {
        execute(manifest, REAL_FILE_OPERATIONS);
    }

    static void execute(DeletionManifest manifest, FileOperations files) throws IOException {
        if (manifest == null) {
            throw new IOException("Manifiesto de borrado no especificado");
        }

        cleanNtdbReferences(manifest.getLogin());

        for (File file : manifest.teamFiles) {
            files.deleteIfExists(file);
        }
        if (manifest.personalLog != null) {
            files.deleteIfExists(manifest.personalLog);
        }

        // El fichero de cuenta se borra siempre el último. Si cualquier paso anterior falla,
        // la cuenta y su marca de solicitud siguen presentes para poder reintentar.
        files.deleteIfExists(manifest.accountFile);
    }

    private static void cleanNtdbReferences(String login) throws IOException {
        synchronized (NtdbBO.class) {
            HashMap<String, String> map = Util.leer_hashmap("NTDB");
            String ownKey = null;
            for (String key : new ArrayList<String>(map.keySet())) {
                if (key.equalsIgnoreCase(login)) {
                    ownKey = key;
                    break;
                }
            }
            if (ownKey != null) {
                map.remove(ownKey);
            }

            for (Entry<String, String> entry : new ArrayList<Entry<String, String>>(map.entrySet())) {
                String cleaned = removeExactLogin(entry.getValue(), login);
                map.put(entry.getKey(), cleaned);
            }
            Util.guardar_hashmap(map, "NTDB");
        }
    }

    private static String removeExactLogin(String csv, String login) {
        if (csv == null || csv.trim().isEmpty()) {
            return "";
        }
        List<String> remaining = new ArrayList<String>();
        for (String token : csv.split(",")) {
            String trimmed = token.trim();
            if (!trimmed.isEmpty() && !trimmed.equalsIgnoreCase(login)) {
                remaining.add(trimmed);
            }
        }
        return String.join(",", remaining);
    }
}
