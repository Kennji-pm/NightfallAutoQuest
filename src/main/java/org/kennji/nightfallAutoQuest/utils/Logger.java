package org.kennji.nightfallAutoQuest.utils;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Logger {

    private final JavaPlugin plugin;
    private java.util.logging.Logger logger;

    public Logger(JavaPlugin plugin) {
        this.plugin = plugin;
        setupLogging();
    }

    private void setupLogging() {
        try {
            File logFolder = new File(plugin.getDataFolder(), "logs");
            if (!logFolder.exists() && !logFolder.mkdirs()) {
                plugin.getLogger().severe("Failed to create logs directory: logs");
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String currentDate = sdf.format(new Date());
            File zipFile = getUniqueZipFile(logFolder, currentDate);

            File latestLog = new File(logFolder, "latest.log");
            File[] logFiles = logFolder.listFiles((dir, name) ->
                    name.endsWith(".log.gz") && name.startsWith(currentDate));

            if (latestLog.exists() || (logFiles != null && logFiles.length > 0)) {
                try (FileOutputStream fos = new FileOutputStream(zipFile, true);
                     ZipOutputStream zos = new ZipOutputStream(fos)) {
                    if (latestLog.exists()) {
                        String entryName = currentDate + "_latest.log.gz";
                        addFileToZip(latestLog, entryName, zos);
                        if (!latestLog.delete()) {
                            plugin.getLogger().warning("Failed to delete latest.log after archiving.");
                        }
                        plugin.getLogger().info("Archived latest.log to: " + zipFile.getName());
                    }

                    if (logFiles != null) {
                        for (File logFile : logFiles) {
                            String entryName = logFile.getName();
                            addFileToZip(logFile, entryName, zos);
                            if (!logFile.delete()) {
                                plugin.getLogger().warning("Failed to delete " + logFile.getName() + " after archiving.");
                            }
                            plugin.getLogger().info("Archived " + logFile.getName() + " to: " + zipFile.getName());
                        }
                    }
                } catch (IOException e) {
                    plugin.getLogger().severe("Failed to archive log files: " + e.getMessage());
                }
            }

            FileHandler fileHandler = new FileHandler(latestLog.getAbsolutePath(), 1024 * 1024, 10, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger = java.util.logging.Logger.getLogger("NightfallAutoQuest");
            logger.setUseParentHandlers(true); // Enable console logging
            logger.addHandler(fileHandler);
            logger.info("Logging initialized to: " + latestLog.getName() + " (with console output)");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to setup logging: " + e.getMessage());
        }
    }

    private File getUniqueZipFile(File logFolder, String currentDate) {
        File zipFile = new File(logFolder, currentDate + ".zip");
        int counter = 1;
        while (zipFile.exists()) {
            zipFile = new File(logFolder, currentDate + "-" + counter + ".zip");
            counter++;
        }
        return zipFile;
    }

    private void addFileToZip(File file, String entryName, ZipOutputStream zos) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    gzip.write(buffer, 0, len);
                }
            }
            ZipEntry zipEntry = new ZipEntry(entryName);
            zos.putNextEntry(zipEntry);
            zos.write(baos.toByteArray());
            zos.closeEntry();
        }
    }

    public java.util.logging.Logger getLogger() {
        return logger;
    }

    public void info(String message) {
        logger.info(message);
    }

    public void warning(String message) {
        logger.warning(message);
    }

    public void severe(String message) {
        logger.severe(message);
    }

    public void log(Level level, String message) {
        logger.log(level, message);
    }

    public void log(Level level, String message, Object param1) {
        logger.log(level, message, param1);
    }

    public void closeHandlers() {
        if (logger != null) {
            for (java.util.logging.Handler handler : logger.getHandlers()) {
                handler.close();
            }
        }
    }
}
