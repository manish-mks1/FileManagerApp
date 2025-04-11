package com.lufick.files.BackgroundTask;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.lufick.files.Callbacks.LoadRecentList;
import com.lufick.files.Callbacks.LoadSearchList;
import com.lufick.files.Adapters.FileItem;
import com.lufick.files.Adapters.RecentFileItem;
import com.lufick.files.Controls.FileManager;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundThread {

    private ExecutorService executorService ;
    private Handler handler ;

    public BackgroundThread() {
        executorService = Executors.newFixedThreadPool(2);
        handler = new Handler(Looper.getMainLooper());
    }

    public void loadRecentFiles(LoadRecentList listener) {
        FileManager fm = new FileManager();
        executorService.execute(() -> {
            List<RecentFileItem> recentFiles = fm.getRecentFiles(Environment.getExternalStorageDirectory());

            handler.post(() -> {
                recentFiles.sort((f1, f2) -> Long.compare(f2.getFile().lastModified(), f1.getFile().lastModified()));
                listener.onLoadRecentList(recentFiles);
            });
        });
    }

    private void fetchFilesRecursive(List<FileItem> allFileList,File directory) {
        if (directory == null || !directory.exists()) return;
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            FileItem item = new FileItem(file);
            allFileList.add(item);

            if (file.isDirectory()) {
                fetchFilesRecursive(allFileList,file);
            }
        }
    }

    public void loadAllFiles(List<FileItem> allFileList, File directory) {
        executorService.execute(() -> {
            allFileList.clear();
            fetchFilesRecursive(allFileList,directory);

        });
    }

    public void searchFiles(List<FileItem> allFileList, ItemAdapter<FileItem> itemAdapter, FastAdapter<FileItem> fastAdapter, String query, LoadSearchList listener) {
        List<FileItem> temp = new ArrayList<>();
        executorService.execute(() -> {
            for (FileItem item : allFileList) {
                if (item.getFile().getName().toLowerCase().contains(query.toLowerCase())) {
                    item.setSearchQuery(query);
                    temp.add(item);
                }
            }
            handler.post(() -> {
                listener.onLoadSearchList(temp);
            });


        });
    }
}
