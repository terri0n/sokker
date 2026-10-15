package com.formulamanager.sokker.auxiliares;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Zip {
	static final int BUFFER = 2048;
	public static void descomnprimir (String archivo) throws Exception {
		BufferedOutputStream dest = null;
		BufferedInputStream is = null;
		ZipEntry entry;
		ZipFile zipfile = new ZipFile(archivo);
		
		Enumeration<? extends ZipEntry> e = zipfile.entries();
		while(e.hasMoreElements()) {
			entry = e.nextElement();
			System.out.println("Extrayendo: " +entry);
			
			if (entry.isDirectory()) {
				new File(entry.getName()).mkdirs();
			} else {
				is = new BufferedInputStream (zipfile.getInputStream(entry));
				int count;
				byte data[] = new byte[BUFFER];
				try {
					FileOutputStream fos = new FileOutputStream(entry.getName());
					dest = new BufferedOutputStream(fos, BUFFER);
					while ((count = is.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, count);
					}
					dest.flush();
					dest.close();
					zipfile.close();
				} catch (FileNotFoundException f) {
					// Si no se puede sobreescribir el archivo saltará esta excepción
					// Si ocurre pasaremos al siguiente archivo
					System.err.println(f + " " + f.getMessage());
				} finally {
					is.close();
				}
			}
		}
	}

	/**
	 * Comprime archivos regulares en un ZIP temporal. Si falla la generación,
	 * conserva la copia anterior en lugar de dejar un ZIP parcial.
	 */
	public static void comprimir(String[] files, String zipFile) throws Exception {
		File destino = new File(zipFile).getAbsoluteFile();
		File temporal = File.createTempFile("backup-", ".tmp", destino.getParentFile());
		boolean completado = false;

		try {
			try (FileOutputStream fos = new FileOutputStream(temporal);
				 ZipOutputStream zos = new ZipOutputStream(fos)) {
				if (files != null) {
					byte[] buffer = new byte[8192];
					for (String ruta : files) {
						if (ruta == null) {
							continue;
						}
						File archivo = new File(ruta);
						if (!archivo.isFile() || !archivo.canRead()) {
							System.err.println("Backup: se omite archivo inexistente o ilegible: " + ruta);
							continue;
						}

						try (FileInputStream fis = new FileInputStream(archivo)) {
							zos.putNextEntry(new ZipEntry(archivo.getName()));
							int longitud;
							while ((longitud = fis.read(buffer)) != -1) {
								zos.write(buffer, 0, longitud);
							}
							zos.closeEntry();
						}
					}
				}
			}

			try {
				Files.move(temporal.toPath(), destino.toPath(),
						StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (java.nio.file.AtomicMoveNotSupportedException e) {
				Files.move(temporal.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			completado = true;
		} finally {
			if (!completado && temporal.exists() && !temporal.delete()) {
				temporal.deleteOnExit();
			}
		}
	}
}
