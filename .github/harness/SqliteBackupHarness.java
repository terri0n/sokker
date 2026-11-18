import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.formulamanager.sokker.persistence.SqliteBackupService;
import com.formulamanager.sokker.persistence.SqliteDatabase;

public final class SqliteBackupHarness {
    public static void main(String[] args) throws Exception {
        Path root=Files.createTempDirectory("sqlite-backup-");
        Path restoreRoot=Files.createTempDirectory("sqlite-restore-");
        File database=root.resolve("factorx.db").toFile();
        byte[] payload=new byte[(int)SqliteBackupService.MAX_EMAIL_PART_BYTES+512*1024];
        for(int i=0;i<payload.length;i++)payload[i]=(byte)(i*31);
        new SqliteDatabase(database,false).initialize();
        try(Connection connection=DriverManager.getConnection("jdbc:sqlite:"+database.getAbsolutePath());
            Statement statement=connection.createStatement()) {
            statement.execute("CREATE TABLE backup_payload(id INTEGER PRIMARY KEY, data BLOB NOT NULL)");
            try(PreparedStatement insert=connection.prepareStatement("INSERT INTO backup_payload(id,data) VALUES(1,?)")) {
                insert.setBytes(1,payload); insert.executeUpdate();
            }
        }

        List<File> parts=SqliteBackupService.createParts(database,root.toFile(),"factorx-backup");
        require(parts.size()==2,"Expected two email fragments, got "+parts.size());
        for(File part:parts)require(part.length()<=SqliteBackupService.MAX_EMAIL_PART_BYTES,"Fragment exceeds email limit: "+part);
        File restored=restoreRoot.resolve("restored.db").toFile();
        SqliteBackupService.restoreParts(parts,restored);
        try(Connection connection=DriverManager.getConnection("jdbc:sqlite:"+restored.getAbsolutePath());
            PreparedStatement query=connection.prepareStatement("SELECT data FROM backup_payload WHERE id=1");
            ResultSet result=query.executeQuery()) {
            require(result.next() && Arrays.equals(payload,result.getBytes(1)),"Restored data differs from the source database");
        }
        File[] contents=root.toFile().listFiles();
        require(contents!=null,"Cannot list backup directory");
        for(File file:contents) {
            require(file.equals(database)||parts.contains(file),"Unexpected temporary file in database directory: "+file);
        }
        Path asistenteRoot=Files.createTempDirectory("sqlite-asistente-backup-");
        File asistenteDatabase=asistenteRoot.resolve("asistente.db").toFile();
        new SqliteDatabase(asistenteDatabase,true).initialize();
        try(Connection connection=DriverManager.getConnection("jdbc:sqlite:"+asistenteDatabase.getAbsolutePath());
            Statement statement=connection.createStatement()) {
            statement.execute("CREATE TABLE backup_payload(id INTEGER PRIMARY KEY, data TEXT NOT NULL)");
            statement.execute("INSERT INTO backup_payload(id,data) VALUES(1,'asistente-only')");
        }
        List<File> asistenteParts=SqliteBackupService.createParts(
                asistenteDatabase,asistenteRoot.toFile(),"asistente-backup");
        require(asistenteParts.size()==1,"Asistente backup fragments were not created independently");
        File restoredAsistente=restoreRoot.resolve("restored-asistente.db").toFile();
        SqliteBackupService.restoreParts(asistenteParts,restoredAsistente);
        try(Connection connection=DriverManager.getConnection("jdbc:sqlite:"+restoredAsistente.getAbsolutePath());
            Statement statement=connection.createStatement();
            ResultSet result=statement.executeQuery("SELECT data FROM backup_payload WHERE id=1")) {
            require(result.next() && "asistente-only".equals(result.getString(1)),
                    "Asistente backup restored FactorX or corrupted data");
        }
        require(!parts.equals(asistenteParts),"Asistente and FactorX backups share fragment files");

        boolean mixedRejected = false;
        List<File> mixedParts = new ArrayList<File>(parts);
        mixedParts.addAll(asistenteParts);
        try {
            SqliteBackupService.restoreParts(mixedParts, restoreRoot.resolve("mixed.db").toFile());
        } catch (IOException expected) {
            mixedRejected = true;
        }
        require(mixedRejected, "Restore accepted fragments from different database generations");

        boolean gapRejected = false;
        try {
            SqliteBackupService.restoreParts(parts.subList(1, parts.size()),
                    restoreRoot.resolve("incomplete.db").toFile());
        } catch (IOException expected) {
            gapRejected = true;
        }
        require(gapRejected, "Restore accepted a backup with a missing first fragment");
        System.out.println("SqliteBackupHarness: OK");
    }
    private static void require(boolean condition,String message) {
        if(!condition)throw new AssertionError(message);
    }
}
