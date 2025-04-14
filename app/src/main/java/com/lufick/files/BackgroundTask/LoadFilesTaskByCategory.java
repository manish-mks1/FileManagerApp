package com.lufick.files.BackgroundTask;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.lufick.files.Callbacks.LoadFileList;
import com.lufick.files.Adapters.FileItem;
import com.lufick.files.Controls.FileManager;
import com.lufick.files.Controls.SortingManager;
import com.lufick.files.FileManagerActivity;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadFilesTaskByCategory {

    private String category;
    private Context context;
    private ItemAdapter<FileItem> itemAdapter;
    private FastAdapter<FileItem> fastAdapter;
    private LoadFileList listener;
    private TextView noFiles;
    private ConstraintLayout progressBar;
    private SharedPreferences sharedPreferences;
    private boolean sortingOrder;
    private String sortingType;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public LoadFilesTaskByCategory(String category, Context context, ItemAdapter<FileItem> itemAdapter, FastAdapter<FileItem> fastAdapter, TextView noFiles, ConstraintLayout progressBar, LoadFileList listener) {
        this.category = category;
        this.context = context;
        this.itemAdapter = itemAdapter;
        this.fastAdapter = fastAdapter;
        this.noFiles = noFiles;
        this.progressBar = progressBar;
        this.listener = listener;
    }

    public void load() {
        progressBar.setVisibility(View.VISIBLE);
        sharedPreferences = context.getSharedPreferences(FileManagerActivity.SORTING_PREF, MODE_PRIVATE);
        sortingOrder = sharedPreferences.getBoolean(FileManagerActivity.SORTING_ORDER, true);
        sortingType = sharedPreferences.getString(FileManagerActivity.SORTING_TYPE, "Name");

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<FileItem> list = loadFiles();
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        itemAdapter.clear();
                        itemAdapter.add(list);
                        fastAdapter.notifyDataSetChanged();
                        listener.onLoad(list);

                        if (itemAdapter.getAdapterItemCount() == 0)
                            noFiles.setVisibility(View.VISIBLE);
                        else
                            noFiles.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private List<FileItem> loadFiles() {
        List<FileItem> list = new ArrayList<>();
        FileManager fm = new FileManager();

        switch (category) {
            case "Images":
                list = fm.getFilesByType(context, "image/");
                break;
            case "Videos":
                list = fm.getFilesByType(context, "video/");
                break;
            case "Documents":
                list = fm.getFilesByType(context, "application/pdf"); // Example: PDFs
                break;
            case "Audio":
                list = fm.getFilesByType(context, "audio/");
                break;
            case "Apps":
                list = fm.findApkFilesUsingMediaStore(context);
                break;
        }
        return list;
    }
}

