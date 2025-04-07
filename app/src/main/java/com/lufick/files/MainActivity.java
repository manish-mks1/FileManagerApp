package com.lufick.files;

import static com.lufick.files.Controls.FileManager.getExternalSDCardPath;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lufick.files.Adapters.QuickAccessItem;
import com.lufick.files.Adapters.RecentFileItem;
import com.lufick.files.Adapters.StorageDeviceItem;
import com.lufick.files.BackgroundTask.BackgroundThread;
import com.lufick.files.Controls.FileManager;
import com.lufick.files.Storage.StorageUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private RecyclerView quickAccessRecycler, storageDevicesRecycler,recentRecycler;

    private ItemAdapter<QuickAccessItem> quickAccessItemAdapter;
    private ItemAdapter<StorageDeviceItem> storageDeviceItemAdapter;
    private ItemAdapter<RecentFileItem> recentItemAdapter;


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





        String internalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();


        quickAccessRecycler = findViewById(R.id.quick_access_recycler);
        storageDevicesRecycler = findViewById(R.id.storage_devices_recycler);
        recentRecycler = findViewById(R.id.recent_recycleView);

        quickAccessItemAdapter = new ItemAdapter<>();
        storageDeviceItemAdapter = new ItemAdapter<>();
        recentItemAdapter = new ItemAdapter<>();


        FastAdapter<QuickAccessItem> quickAccessFastAdapter = FastAdapter.with(quickAccessItemAdapter);
        FastAdapter<StorageDeviceItem> storageDeviceFastAdapter = FastAdapter.with(storageDeviceItemAdapter);
        FastAdapter<RecentFileItem> recentFastadapter = FastAdapter.with(recentItemAdapter);

        setupQuickAccessRecycler(quickAccessFastAdapter);
        setupStorageDevicesRecycler(storageDeviceFastAdapter);
        setupRecentFileRecycler(recentFastadapter);


        recentFastadapter.setOnClickListener((view, recentFileItemIAdapter, recentFileItem, integer) -> {
            FileManager fm = new FileManager();
            fm.openFile(MainActivity.this,recentFileItem.getFile());
            return true;
        });

        storageDeviceFastAdapter.setOnClickListener((view, storageDeviceItemIAdapter, item, integer) -> {
            Intent intent = new Intent(this,FileManagerActivity.class);
            if(item.deviceName.equals("Internal Storage")){
                intent.putExtra("Storage",internalStoragePath);
                startActivity(intent);
            }else if(item.deviceName.equals("SD Card")){
                File sdCard = getExternalSDCardPath(this);
                if (sdCard != null && sdCard.isDirectory()) {
                    intent.putExtra("Storage",sdCard.getAbsolutePath());
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "SD Card not found", Toast.LENGTH_SHORT).show();
                }

            }
            return true;
        });

        quickAccessFastAdapter.setOnClickListener((view, adapter, item, position) -> {
            openFileCategory(item.getTitle());
            return true;
        });

        BackgroundThread bt = new BackgroundThread();
        bt.loadRecentFiles(recentItemAdapter);

        loadQuickAccessItems();
        loadStorageDevices();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            } else {
                bt.loadRecentFiles(recentItemAdapter);
                loadStorageDevices();
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            } else {
                bt.loadRecentFiles(recentItemAdapter);
                loadStorageDevices();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        BackgroundThread bt = new BackgroundThread();
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bt.loadRecentFiles(recentItemAdapter);
                loadStorageDevices();
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

    private void setupQuickAccessRecycler(FastAdapter<QuickAccessItem> adapter) {
        quickAccessRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        quickAccessRecycler.setAdapter(adapter);
    }

    private void setupStorageDevicesRecycler(FastAdapter<StorageDeviceItem> adapter) {
        storageDevicesRecycler.setLayoutManager(new LinearLayoutManager(this));
        storageDevicesRecycler.setAdapter(adapter);
    }
    private void setupRecentFileRecycler(FastAdapter<RecentFileItem> adapter) {
        recentRecycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        recentRecycler.setAdapter(adapter);
    }

    private void loadQuickAccessItems() {
        List<QuickAccessItem> items = new ArrayList<>();
        items.add(new QuickAccessItem("Images", R.drawable.photoes));
        items.add(new QuickAccessItem("Videos", R.drawable.videos));
        items.add(new QuickAccessItem("Audio", R.drawable.music_file_icon));
        items.add(new QuickAccessItem("Documents", R.drawable.document_file_icon));
        items.add(new QuickAccessItem("Downloads", R.drawable.attachment));
        items.add(new QuickAccessItem("Apps", R.drawable.folder));

        quickAccessItemAdapter.set(items);
    }
    private void openFileCategory(String category) {
        Intent intent = new Intent(this, FileManagerActivity.class);
        if(category.equals("Downloads"))
            intent.putExtra("Storage", Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download");
        else
            intent.putExtra("FILE_CATEGORY", category);
        startActivity(intent);
    }

    private void loadStorageDevices() {
        List<StorageDeviceItem> devices = new ArrayList<>();
        String storageInfo = StorageUtils.getInternalStorageInfo(MainActivity.this);
        devices.add(new StorageDeviceItem(R.drawable.mobile_ic,"Internal Storage",storageInfo ));
        String sdInfo = StorageUtils.getSDCardStorageInfo(this);
        devices.add(new StorageDeviceItem(R.drawable.sd_card_ic,"SD Card", sdInfo));

        storageDeviceItemAdapter.set(devices);


    }
}