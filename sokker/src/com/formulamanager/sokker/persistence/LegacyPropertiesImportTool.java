package com.formulamanager.sokker.persistence;

import java.io.File;

/**
 * Explicit command-line entry point for inspecting a legacy import. Webapp startup
 * runs the complete one-time migration, including normalized DAO imports and source retirement.
 *
 * Usage: LegacyPropertiesImportTool <legacy-data-directory> <sqlite-file>
 */
public final class LegacyPropertiesImportTool {
    private LegacyPropertiesImportTool() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException(
                    "Usage: LegacyPropertiesImportTool <legacy-data-directory> <sqlite-file>");
        }
        LegacyPropertiesImporter.ImportReport report =
                new LegacyPropertiesImporter(
                        new SqliteDatabase(new File(args[1])),
                        new File(args[0])).importAll();
        System.out.println("Discovered properties files: " + report.getDiscovered());
        System.out.println("Imported/updated: " + report.getImported());
        System.out.println("Unchanged: " + report.getUnchanged());
    }
}
