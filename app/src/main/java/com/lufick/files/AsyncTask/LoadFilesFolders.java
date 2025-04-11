package com.lufick.files.AsyncTask;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.lufick.files.Callbacks.LoadFilteredList;
import com.lufick.files.Adapters.FileItem;
import com.lufick.files.Controls.SortingManager;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LoadFilesFolders extends AsyncTask<Void, Void, List<FileItem>> {
    private File directory;
    Context context;
    ItemAdapter<FileItem> itemAdapter;
    FastAdapter<FileItem> fastAdapter;
    TextView noFiles;
    ConstraintLayout progressBar;

    LoadFilteredList listener;

    SharedPreferences sharedPreferences;
    boolean sortingOrder;
    String sortingType;

    public LoadFilesFolders(File directory, Context context, ItemAdapter<FileItem> itemAdapter, FastAdapter<FileItem> fastAdapter, TextView noFiles, ConstraintLayout progressBar,LoadFilteredList listener) {
        this.directory = directory;
        this.context = context;
        this.itemAdapter = itemAdapter;
        this.fastAdapter = fastAdapter;
        this.noFiles = noFiles;
        this.progressBar = progressBar;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressBar.setVisibility(View.VISIBLE);
        sharedPreferences = context.getSharedPreferences("Files_sorting_pref",MODE_PRIVATE);
        sortingOrder = sharedPreferences.getBoolean("sortingOrder",true);
        sortingType = sharedPreferences.getString("sortingType","Name");

    }

    @Override
    protected List<FileItem> doInBackground(Void... voids) {
        List<FileItem> list = new ArrayList<>();
        if (directory == null || !directory.isDirectory()) return null;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                list.add(new FileItem(file));
            }
        }
        return list;
    }

    @Override
    protected void onPostExecute(List<FileItem> fileItems) {
        super.onPostExecute(fileItems);

        new SortingManager().sortBy(itemAdapter,fastAdapter,fileItems,sortingType,sortingOrder);
        if (fileItems != null) {
            itemAdapter.clear();
            itemAdapter.add(fileItems);
            fastAdapter.notifyDataSetChanged();
            listener.onLoadFilteredList(fileItems);


            progressBar.setVisibility(View.GONE);
            if(itemAdapter.getAdapterItemCount()==0)
                noFiles.setVisibility(View.VISIBLE);
            else
                noFiles.setVisibility(View.GONE);
        }

    }
}
