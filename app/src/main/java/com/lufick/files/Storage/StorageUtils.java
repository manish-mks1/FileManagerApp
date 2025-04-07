package com.lufick.files.Storage;

import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class StorageUtils {

    // Fetch internal storage info
    public static String getInternalStorageInfo(Context context) {
        File internalStorage = Environment.getDataDirectory();
        return getStorageDetails(context);
    }

    // Fetch SD card storage info (if exists)
    public static String getSDCardStorageInfo(Context context) {
        File[] externalDirs = context.getExternalFilesDirs(null);

        // Loop through available external directories
        for (File file : externalDirs) {
            if (file != null && Environment.isExternalStorageRemovable(file)) {
                return getStorageDetails(context);
            }
        }

        return "Not Inserted";
    }

    // Core method to fetch storage details
    private static String getStorageDetails(Context context) {
        StorageStatsManager storageStatsManager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
        }
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

        long totalBytes = 0L;
        long freeBytes = 0L;
        long usedBytes = 0L;

        try {
            if (storageManager != null) {
                List<StorageVolume> storageVolumes = storageManager.getStorageVolumes();
                for (StorageVolume volume : storageVolumes) {
                    String uuidStr = volume.getUuid();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        UUID uuid = uuidStr == null ? StorageManager.UUID_DEFAULT : UUID.fromString(uuidStr);
                        totalBytes += storageStatsManager.getTotalBytes(uuid);
                        freeBytes += storageStatsManager.getFreeBytes(uuid);
                    }
                }
                usedBytes = totalBytes - freeBytes;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Used: " + formatSize(usedBytes) + " / Total: " + formatSize(totalBytes);
    }

    private static String formatSize(long size) {
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double fileSize = size;

        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024.0;
            unitIndex++;
        }

        if (unitIndex >= 2) { // MB or above
            return String.format("%.0f %s", fileSize, units[unitIndex]);
        } else {
            return String.format("%.2f %s", fileSize, units[unitIndex]);
        }
    }
}


