package com.lufick.files.AsyncTask;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.lufick.files.Adapters.Callbacks.LoadFilteredList;
import com.lufick.files.Adapters.FileItem;
import com.lufick.files.Controls.FileManager;
import com.lufick.files.Controls.SortingManager;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class LoadFilesTaskByCategory extends AsyncTask<Void, Void, List<FileItem>> {

    private String category;
    private Context context;
    private ItemAdapter<FileItem> itemAdapter;
    private FastAdapter<FileItem> fastAdapter;
    LoadFilteredList listener;
    private TextView noFiles;
    private ConstraintLayout progressBar;
    private SharedPreferences sharedPreferences;
    private boolean sortingOrder;
    private String sortingType;

    public LoadFilesTaskByCategory(String category, Context context, ItemAdapter<FileItem> itemAdapter, FastAdapter<FileItem> fastAdapter, TextView noFiles, ConstraintLayout progressBar,LoadFilteredList listener) {
        this.category = category;
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
        sortingOrder = sharedPreferences.getBoolean("sortingOrder",false);
        sortingType = sharedPreferences.getString("sortingType","Name");
    }

    @Override
    protected List<FileItem> doInBackground(Void... voids) {
        List<FileItem> list = new ArrayList<>();

        FileManager fm = new FileManager();

        switch (category) {
            case "Images":
                list = fm.getFilesByType(context,"image/");
                break;
            case "Videos":
                list = fm.getFilesByType(context,"video/");
                break;
            case "Documents":
                list = fm.getFilesByType(context,"application/pdf"); // Example: PDFs
                break;
            case "Audio":
                list = fm.getFilesByType(context,"audio/");
                break;
            case "Downloads":
                list = fm.getDownloadFiles(context);
                break;
            case "Apps":
                list = fm.getInstalledApps(context);
                break;
        }
        return list;
    }

    @Override
    protected void onPostExecute(List<FileItem> filteredFiles) {
        super.onPostExecute(filteredFiles);

        itemAdapter.clear();
        new SortingManager().sortBy(itemAdapter,fastAdapter,filteredFiles,sortingType,sortingOrder);
        itemAdapter.add(filteredFiles);
        fastAdapter.notifyDataSetChanged();
        listener.onLoadFilteredList(filteredFiles);

        progressBar.setVisibility(View.GONE);
        if(itemAdapter.getAdapterItemCount()==0)
            noFiles.setVisibility(View.VISIBLE);
        else
            noFiles.setVisibility(View.GONE);
    }
}
