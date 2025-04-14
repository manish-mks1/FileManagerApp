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
import android.text.format.DateUtils;
import android.text.format.Formatter;

import com.lufick.files.Enumeration.FileCategory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StorageUtils {

    // Fetch internal storage info
    public static String getInternalStorageInfo(Context context) {
        String internalStorage = Environment.getDataDirectory().getAbsolutePath();
        return getStorageDetails(context, internalStorage);
    }

    // Fetch SD card storage info (if exists)
    public static String getSDCardStorageInfo(Context context) {
        File[] externalDirs = context.getExternalFilesDirs(null);

        for (File file : externalDirs) {
            if (file != null && Environment.isExternalStorageRemovable(file)) {
                return getStorageDetails(context,file.getAbsolutePath());
            }
        }
        return "Not Inserted";
    }

    public static String getStorageDetails(Context context, String file_path) {
        StatFs statFs = new StatFs(file_path);
        long totalBytes = statFs.getTotalBytes();
        long freeBytes = statFs.getAvailableBytes();
        long usedBytes = totalBytes - freeBytes;

        return "Used: " + Formatter.formatFileSize(context, usedBytes) + " / Total: " + Formatter.formatFileSize(context, totalBytes);
    }

    public static String getTimeAgoFormat(File file) {
        long lastModified = file.lastModified();
        return (String) DateUtils.getRelativeTimeSpanString(
                lastModified,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        );
    }

    public static String formatSize(Context context, long sizeInBytes) {
        return Formatter.formatFileSize(context, sizeInBytes);
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


