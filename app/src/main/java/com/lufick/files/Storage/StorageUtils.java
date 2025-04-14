package com.lufick.files.Storage;

import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;

import com.lufick.files.Enumeration.FileCategory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public static String formatSize(long size) {
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

    public Map<FileCategory, Long> getUsedStorageByCategories(Context context) {
        Map<FileCategory, Long> categorySizes = new HashMap<>();

        for (FileCategory category : FileCategory.values()) {
            long totalSize = 0;

            switch (category) {
                case IMAGES:
                    totalSize = getTotalSizeByUriAndMime(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null);
                    break;
                case VIDEOS:
                    totalSize = getTotalSizeByUriAndMime(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null);
                    break;
                case AUDIO:
                    totalSize = getTotalSizeByUriAndMime(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null);
                    break;
                case DOCUMENTS:
                case APK:
                    totalSize = getTotalSizeByMimeArray(context, category.getMimeTypes());
                    break;
                case DOWNLOADS:
                    totalSize = getTotalSizeByDownloads(context);
                    break;
            }

            categorySizes.put(category, totalSize);
        }

        return categorySizes;
    }

    private long getTotalSizeByUriAndMime(Context context, Uri uri, String mimeType) {
        long totalSize = 0;
        String selection = (mimeType != null) ? MediaStore.MediaColumns.MIME_TYPE + " = ?" : null;
        String[] selectionArgs = (mimeType != null) ? new String[]{mimeType} : null;
        String[] projection = {MediaStore.MediaColumns.SIZE};

        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
            if (cursor != null) {
                int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE);
                while (cursor.moveToNext()) {
                    totalSize += cursor.getLong(sizeIndex);
                }
            }
        }
        return totalSize;
    }

    private long getTotalSizeByMimeArray(Context context, String[] mimeTypes) {
        Uri uri = MediaStore.Files.getContentUri("external");
        if (mimeTypes == null) return 0;

        StringBuilder selectionBuilder = new StringBuilder();
        for (int i = 0; i < mimeTypes.length; i++) {
            selectionBuilder.append(MediaStore.Files.FileColumns.MIME_TYPE).append(" = ?");
            if (i < mimeTypes.length - 1) {
                selectionBuilder.append(" OR ");
            }
        }

        String[] projection = {MediaStore.Files.FileColumns.SIZE};

        long totalSize = 0;
        try (Cursor cursor = context.getContentResolver().query(uri, projection, selectionBuilder.toString(), mimeTypes, null)) {
            if (cursor != null) {
                int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
                while (cursor.moveToNext()) {
                    totalSize += cursor.getLong(sizeIndex);
                }
            }
        }
        return totalSize;
    }

    private long getTotalSizeByDownloads(Context context) {
        Uri uri = MediaStore.Files.getContentUri("external");
        String[] projection = {MediaStore.Files.FileColumns.SIZE};
        String selection = MediaStore.Files.FileColumns.RELATIVE_PATH + " LIKE ?";
        String[] selectionArgs = new String[]{"%Download%"};

        long totalSize = 0;
        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
            if (cursor != null) {
                int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
                while (cursor.moveToNext()) {
                    totalSize += cursor.getLong(sizeIndex);
                }
            }
        }
        return totalSize;
    }
}


