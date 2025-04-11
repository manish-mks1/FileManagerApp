package com.lufick.files.Controls;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.lufick.files.Adapters.FileItem;
import com.lufick.files.Adapters.RecentFileItem;
import com.lufick.files.R;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.select.SelectExtension;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FileManager {
    public static File getExternalSDCardPath(Context context) {
        File[] externalDirs = context.getExternalFilesDirs(null);

        for (File file : externalDirs) {
            if (file != null && !file.getAbsolutePath().contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                return file.getParentFile().getParentFile().getParentFile().getParentFile();
            }
        }
        return null;
    }

    public void refreshList(List<FileItem> filteredList, ItemAdapter<FileItem> itemAdapter, FastAdapter<FileItem> fastAdapter) {
        itemAdapter.clear();
        itemAdapter.add(filteredList);
        fastAdapter.notifyDataSetChanged();
    }

    public static String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        int exp = (int) (Math.log(size) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "B";
        return String.format("%.1f %s", size / Math.pow(1024, exp), pre);
    }




    public void openFile(Context context,File file) {
        try {
            String mimeType = getMimeType(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri fileUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName()+".provider",
                    file
            );

            intent.setDataAndType(fileUri, mimeType);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Cannot open this file", Toast.LENGTH_SHORT).show();
        }
    }

    public static String getMimeType(File file) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        return type;
    }

    public List<FileItem> getFilesByType(Context context, String mimeType) {
        List<FileItem> fileList = new ArrayList<>();
        Uri uri = MediaStore.Files.getContentUri("external");

        String selection = MediaStore.Files.FileColumns.MIME_TYPE + " LIKE ?";
        String[] selectionArgs = new String[]{mimeType + "%"};

        String[] projection = {
                MediaStore.Files.FileColumns.DATA
        };

        Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
                File file = new File(filePath);
                if (file.exists()) {
                    fileList.add(new FileItem(file));
                }
            }
            cursor.close();
        }
        return fileList;
    }
    public List<FileItem> getDownloadFiles(Context context) {
        List<FileItem> fileList = new ArrayList<>();
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = MediaStore.Downloads.getContentUri("external");
        }

        String[] projection = {
                MediaStore.Downloads.DATA
        };

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Downloads.DATA));
                File file = new File(filePath);
                if (file.exists()) {
                    fileList.add(new FileItem(file));
                }
            }
            cursor.close();
        }
        return fileList;
    }
    public List<FileItem> getInstalledApps(Context context) {
        List<FileItem> appList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : apps) {
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) { // Exclude system apps
                File apkFile = new File(app.sourceDir);
                if (apkFile.exists()) {
                    appList.add(new FileItem(apkFile));
                }
            }
        }
        return appList;
    }

    public void renameSelectedFolderFiles(ItemAdapter<FileItem> itemAdapter, FastAdapter<FileItem> fastAdapter, SelectExtension<FileItem> selectExtension, String newName) {
        Set<Integer> selectedPosition = selectExtension.getSelections();
        for(Integer i : selectedPosition){
            File file = itemAdapter.getAdapterItem(i).getFile();
            if(file.isDirectory()){
                if (file.renameTo(new File(file.getParent(), newName))) {
                    Log.e("rename", "renameSelectedFolderFiles: ");
                }
            }else {
                String extension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
                if (file.renameTo(new File(file.getParent(), newName + "." + extension))) {
                    Log.e("rename", "renameSelectedFolderFiles: ");
                }
            }
            fastAdapter.notifyItemChanged(i);
        }
        selectExtension.deselect();
    }
    public void renameFolderOrFile(File file, String newName) {
        if(file.isDirectory()){
            if (file.renameTo(new File(file.getParent(), newName))) {
                Log.e("rename", "renameSelectedFolderFiles: ");
            }
        }else {
            String extension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
            if (file.renameTo(new File(file.getParent(), newName + "." + extension))) {
                Log.e("rename", "renameSelectedFolderFiles: ");
            }
        }
    }

    public void deleteSelectedFiles(ItemAdapter<FileItem> itemAdapter, SelectExtension<FileItem> selectExtension) {
        Set<Integer> selectedPositions = selectExtension.getSelections();
        List<FileItem> selectedItems = new ArrayList<>();

        for (int pos : selectedPositions) {
            selectedItems.add(itemAdapter.getAdapterItem(pos));
            File file = new File(itemAdapter.getAdapterItem(pos).getFile().getAbsolutePath());
            file.delete();
            itemAdapter.remove(pos);
        }

        selectExtension.deselect();
    }
    public void copyFileOrFolder(String sourcePath, String destinationPath){
        File sourceFile = new File(sourcePath);
        File destFile = new File(destinationPath, sourceFile.getName());

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void copySelectedFiles(ItemAdapter<FileItem> itemAdapter, SelectExtension<FileItem> selectExtension, String destinationPath){
        Set<Integer> selectedPositions = selectExtension.getSelections();

        for (int pos : selectedPositions) {
            FileItem fileItem = itemAdapter.getAdapterItem(pos);
            File sourceFile = new File(fileItem.getFile().getAbsolutePath());
            File destFile = new File(destinationPath, sourceFile.getName());

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void moveFileOrFolder(ItemAdapter<FileItem> itemAdapter, int pos, String sourcePath, String destinationPath){
        File sourceFile = new File(sourcePath);
        File destFile = new File(destinationPath, sourceFile.getName());

        if (sourceFile.renameTo(destFile)) {
            itemAdapter.remove(pos);
        }
    }
    public void moveSelectedFiles(ItemAdapter<FileItem> itemAdapter, SelectExtension<FileItem> selectExtension, String destinationPath) {
        Set<Integer> selectedPositions = selectExtension.getSelections();

        for (int pos : selectedPositions) {
            FileItem fileItem = itemAdapter.getAdapterItem(pos);
            File sourceFile = new File(fileItem.getFile().getAbsolutePath());
            File destFile = new File(destinationPath, sourceFile.getName());

            if (sourceFile.renameTo(destFile)) {
                itemAdapter.remove(pos);
            }
        }
        selectExtension.deselect();
    }
    public void deleteFileOrFolder(File file) {
        file.delete();
    }

    public List<RecentFileItem> getRecentFiles(File directory) {
        List<RecentFileItem> recentFiles = new ArrayList<>();

        if (directory == null || !directory.exists()) return recentFiles;

        File[] files = directory.listFiles();
        if (files == null) return recentFiles;

        long twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000); // Last 24 hours

        for (File file : files) {
            if (file.isDirectory()) {
                recentFiles.addAll(getRecentFiles(file)); // Recursive call for subdirectories
            }else{
                String mimeType = getMimeType(file);
                if(mimeType!= null && !mimeType.isEmpty()) {
                    if (mimeType.startsWith("image/") || mimeType.startsWith("video/") || mimeType.startsWith("audio/") || mimeType.startsWith("application/pdf")) {
                        if (file.lastModified() >= twentyFourHoursAgo) { // Check if modified in the last 24 hours
                            recentFiles.add(new RecentFileItem(file));
                        }
                    }
                }
            }
        }
        return recentFiles;
    }

    public void setFileIcon(ImageView icon, View itemView,String mimeType,File file){
        if (mimeType == null) {
            mimeType = URLConnection.guessContentTypeFromName(file.getName());
        }

        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                Glide.with(itemView.getContext())
                        .load(file)
                        .placeholder(R.drawable.image_ic)
                        .error(R.drawable.baseline_broken_image_24)
                        .into(icon);
            } else if (mimeType.startsWith("video/")) {
                icon.setImageResource(R.drawable.videos);
            } else if (mimeType.startsWith("audio/")) {
                icon.setImageResource(R.drawable.music_file_icon);
            } else if (mimeType.equals("application/pdf")) {
                icon.setImageResource(R.drawable.pdf);
            } else if (mimeType.startsWith("text/")) {
                icon.setImageResource(R.drawable.file_outlined);
            } else {
                icon.setImageResource(R.drawable.file_outlined);
            }
        } else {
            icon.setImageResource(R.drawable.file_outlined);
        }
    }


    public void addFolder(View view,File directory,String newFolderName){
        File folder = new File(directory,newFolderName);
        if(!folder.exists()){
            if(folder.mkdir()){
                Snackbar.make(view,"Folder created.", Snackbar.LENGTH_SHORT).show();
            }
        }else{
                Snackbar.make(view,"Folder Already Exists.", Snackbar.LENGTH_SHORT).show();
        }
    }
}