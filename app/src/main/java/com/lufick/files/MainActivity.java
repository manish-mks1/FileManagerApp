package com.lufick.files;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lufick.files.Adapters.MainItemAdapter;
import com.lufick.files.Adapters.QuickAccessItem;
import com.lufick.files.Adapters.RecentFileItem;
import com.lufick.files.Adapters.StorageDeviceItem;
import com.lufick.files.BackgroundTask.BackgroundThread;
import com.lufick.files.Callbacks.LoadRecentList;
import com.lufick.files.Enumeration.CategoryType;
import com.lufick.files.Enumeration.FileCategory;
import com.lufick.files.Storage.StorageUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private RecyclerView recyclerview;
    FastAdapter<MainItemAdapter<? extends AbstractItem>> fastadapter;
    private ItemAdapter<MainItemAdapter<? extends AbstractItem>> itemAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new ItemAdapter<>();

        fastadapter = FastAdapter.with(itemAdapter);

        recyclerview.setAdapter(fastadapter);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            } else {
                loadData();
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            } else {
                loadData();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadData();

            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Needed")
                        .setMessage("This app requires storage access to function properly.")
                        .setPositiveButton("Grant", (dialog, which) ->
                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100))
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            } else {
                Toast.makeText(this, "Permission denied. Please enable from settings.", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void loadData(){
        loadRecentFile();
        loadQuickAccessItems();
        loadStorageDevices();
    }

    private void loadRecentFile(){
        BackgroundThread bt = new BackgroundThread();
        bt.loadRecentFiles(new LoadRecentList() {
            @Override
            public void onLoadRecentList(List<RecentFileItem> recentList) {
                itemAdapter.add(0, new MainItemAdapter<RecentFileItem>(MainActivity.this, CategoryType.Recent.name(),recentList));
            }
        });
    }


    private void loadQuickAccessItems() {
        StorageUtils storageUtils = new StorageUtils();
        Map<FileCategory, Long> storageUsage = storageUtils.getUsedStorageByCategories(MainActivity.this);

        List<QuickAccessItem> items = new ArrayList<>();
        items.add(new QuickAccessItem("Images",StorageUtils.formatSize(MainActivity.this,storageUsage.get(FileCategory.IMAGES)), R.drawable.image_ic));
        items.add(new QuickAccessItem("Videos", StorageUtils.formatSize(MainActivity.this,storageUsage.get(FileCategory.VIDEOS)), R.drawable.video_ic));
        items.add(new QuickAccessItem("Audio", StorageUtils.formatSize(MainActivity.this,storageUsage.get(FileCategory.AUDIO)), R.drawable.ic_audio));
        items.add(new QuickAccessItem("Documents", StorageUtils.formatSize(MainActivity.this,storageUsage.get(FileCategory.DOCUMENTS)), R.drawable.document_file_icon));
        items.add(new QuickAccessItem("Downloads", StorageUtils.formatSize(MainActivity.this,storageUsage.get(FileCategory.DOWNLOADS)), R.drawable.download_ic));
        items.add(new QuickAccessItem("Apps", StorageUtils.formatSize(MainActivity.this,storageUsage.get(FileCategory.APK)), R.drawable.folder));
        itemAdapter.add(new MainItemAdapter<QuickAccessItem>(this,CategoryType.Category.name(), items));

        fastadapter.notifyDataSetChanged();
    }


    private void loadStorageDevices() {
        List<StorageDeviceItem> devices = new ArrayList<>();
        String storageInfo = StorageUtils.getInternalStorageInfo(MainActivity.this);
        devices.add(new StorageDeviceItem(R.drawable.mobile_ic,"Internal Storage",storageInfo ));
        String sdInfo = StorageUtils.getSDCardStorageInfo(this);
        if (sdInfo != null && !sdInfo.isEmpty()) {
            devices.add(new StorageDeviceItem(R.drawable.sd_card_ic, "SD Card", sdInfo));
        }

        itemAdapter.add(new MainItemAdapter<StorageDeviceItem>(this,CategoryType.Storage.name(), devices));

        fastadapter.notifyDataSetChanged();

    }
}