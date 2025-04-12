package com.lufick.files.BackgroundTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.lufick.files.Adapters.FileItem;
import com.lufick.files.Callbacks.LoadFilteredList;
import com.lufick.files.Controls.SortingManager;
import com.lufick.files.FileManagerActivity;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadFilesFolders {

    private final File directory;
    private final Context context;
    private final ItemAdapter<FileItem> itemAdapter;
    private final FastAdapter<FileItem> fastAdapter;
    private final TextView noFiles;
    private final ConstraintLayout progressBar;
    private final LoadFilteredList listener;

    private final SharedPreferences sharedPreferences;
    private final boolean sortingOrder;
    private final String sortingType;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public LoadFilesFolders(File directory,
                            Context context,
                            ItemAdapter<FileItem> itemAdapter,
                            FastAdapter<FileItem> fastAdapter,
                            TextView noFiles,
                            ConstraintLayout progressBar,
                            LoadFilteredList listener) {
        this.directory = directory;
        this.context = context;
        this.itemAdapter = itemAdapter;
        this.fastAdapter = fastAdapter;
        this.noFiles = noFiles;
        this.progressBar = progressBar;
        this.listener = listener;

        sharedPreferences = context.getSharedPreferences(FileManagerActivity.SORTING_PREF, Context.MODE_PRIVATE);
        sortingOrder = sharedPreferences.getBoolean(FileManagerActivity.SORTING_ORDER, true);
        sortingType = sharedPreferences.getString(FileManagerActivity.SORTING_TYPE, "Name");
    }

    public void load() {
        progressBar.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            List<FileItem> fileItems = loadFiles();

            handler.post(() -> {
                itemAdapter.clear();
                itemAdapter.add(fileItems);
                fastAdapter.notifyDataSetChanged();
                listener.onLoadFilteredList(fileItems);

                new SortingManager().sortBy(itemAdapter, fastAdapter, fileItems, sortingType, sortingOrder);

                progressBar.setVisibility(View.GONE);
                noFiles.setVisibility(itemAdapter.getAdapterItemCount() == 0 ? View.VISIBLE : View.GONE);
            });
        });
    }

    private List<FileItem> loadFiles() {
        List<FileItem> list = new ArrayList<>();
        if (directory != null && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    list.add(new FileItem(file));
                }
            }
        }
        return list;
    }
}
