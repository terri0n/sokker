package com.formulamanager.sokker.auxiliares;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

	public static void comprimir(String[] files, String zipFile) throws Exception {
		FileOutputStream fos = new FileOutputStream(zipFile);
		ZipOutputStream zos = new ZipOutputStream(fos);
		
		try {
			for (int i = 0; i < files.length; i++) {
				File archivo  = new File(files[i]);
				if (!archivo.isDirectory()) {
					FileInputStream fis = new FileInputStream(files[i]);
					try {
						// create byte buffer
						byte[] buffer = new byte[1024];
		
						ZipEntry zipEntry = new ZipEntry(archivo.getName());
						zipEntry.setMethod(ZipEntry.DEFLATED);
						zos.putNextEntry(zipEntry);
						
						int length;
						while ((length = fis.read(buffer)) > 0) {
							zos.write(buffer, 0, length);
						}
		
					} catch (IOException ioe) {
						System.out.println("IOException :" + ioe);
					} finally {
						// close the InputStream
						fis.close();
						zos.closeEntry();
					}
				}
			}
		} finally {
			// close the ZipOutputStream
			zos.close();
		}
	}
}
