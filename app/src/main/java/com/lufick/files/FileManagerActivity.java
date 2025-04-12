package com.lufick.files;

import static com.lufick.files.Enumeration.ActionType.ADD_FOLDER;
import static com.lufick.files.Enumeration.ActionType.DELETE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lufick.files.Adapters.BreadcrumbItem;
import com.lufick.files.Callbacks.LoadAlertDialogBox;
import com.lufick.files.Callbacks.LoadFilteredList;
import com.lufick.files.Callbacks.LoadSearchList;
import com.lufick.files.Adapters.FileItem;
import com.lufick.files.BackgroundTask.LoadFilesFolders;
import com.lufick.files.BackgroundTask.LoadFilesTaskByCategory;
import com.lufick.files.BackgroundTask.BackgroundThread;
import com.lufick.files.Controls.FileManager;
import com.lufick.files.Controls.SortingManager;
import com.lufick.files.Enumeration.ActionType;
import com.lufick.files.Enumeration.SortingType;
import com.lufick.files.Fragments.FolderPickerDialog;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.mikepenz.fastadapter.select.SelectExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileManagerActivity extends AppCompatActivity {

    public static final String SORTING_PREF = "Files_sorting_pref";
    public static final String SORTING_TYPE = "sortingType";
    public static final String SORTING_ORDER = "sortingOrder";
    File currentDirectory;
    File internalStorage;
    SelectExtension<FileItem> selectExtension;
    private RecyclerView recyclerView, breadcrumbRecyclerView;
    File storage ;
    String fileCategory;
    List<BreadcrumbItem> breadcrumbs;

    private FastAdapter<BreadcrumbItem> breadcrumbFastAdapter;
    private ItemAdapter<BreadcrumbItem> breadcrumbItemAdapter;

    List<FileItem> itemList;
    private ItemAdapter<FileItem> itemAdapter;
    private FastAdapter<FileItem> fastAdapter;

    FileManager fm;
    BackgroundThread bt;
    SortingManager sm;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean sortingOrder;
    String sortingType;
    int count;

    FloatingActionButton addFolderBtn;
    List<FileItem> allFileList;
    TextView sortingTypeName, homeBtn;
    ImageView sortingAccDesc;
    TextView selectedCount, noFiles;
    ConstraintLayout selectionBar, progressBar;
    LinearLayout  selectionBottomBar, deleteBtn,copyBtn,moveBtn,renameBtn, sortingTypeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_file_manager);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initiateUI();
        fastadapterListener();
        clickListener();
    }
    private void clickListener() {
        addFolderBtn.setOnClickListener(view -> {
            fm.showAlertDialog(FileManagerActivity.this, "Create Folder", null, "Create", "Cancel", ADD_FOLDER, FileManager.NEW_FOLDER, new LoadAlertDialogBox() {
                @Override
                public void onLoadAlertDialog(String newName) {
                    fm.addFolder(view,currentDirectory,newName);
                    loadFiles(currentDirectory);
                }
            });
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFiles(internalStorage);
            }
        });
        breadcrumbFastAdapter.addEventHook(new ClickEventHook<BreadcrumbItem>() {
            @Nullable
            @Override
            public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                if(viewHolder instanceof BreadcrumbItem.ViewHolder){
                    return ((BreadcrumbItem.ViewHolder)viewHolder).breadcrumbText;
                }
                return null;
            }

            @Override
            public void onClick(@NonNull View view, int i, @NonNull FastAdapter<BreadcrumbItem> fastAdapter, @NonNull BreadcrumbItem item) {
                loadFiles(item.getDirectory());
            }
        });
        deleteBtn.setOnClickListener(v -> {
            fm.showAlertDialog(FileManagerActivity.this, "Delete File(s)/Folder(s)", "Are you sure you want to delete this item?", "OK", "Cancel", ADD_FOLDER, null, new LoadAlertDialogBox() {
                @Override
                public void onLoadAlertDialog(String newName) {
                    fm.deleteSelectedFiles(itemAdapter,selectExtension);
                    fastAdapter.notifyDataSetChanged();
                    selectionBarVisibility(false);

                }
            });
        });
        renameBtn.setOnClickListener(v -> {
            if(!selectExtension.getSelections().isEmpty()){
                FileItem item = itemAdapter.getAdapterItem(selectExtension.getSelections().iterator().next());
                fm.showAlertDialog(FileManagerActivity.this, "Rename File/Folder", null, "Rename", "Cancel", ADD_FOLDER, FileManager.NEW_FOLDER, new LoadAlertDialogBox() {
                    @Override
                    public void onLoadAlertDialog(String newName) {
                        fm.renameFolderOrFile(item.getFile(),newName);
                        fastAdapter.notifyDataSetChanged();
                        selectionBarVisibility(false);
                    }
                });
            }

        });

        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!selectExtension.getSelections().isEmpty()){
                    new FolderPickerDialog(selectedFolderPath -> {
                        fm.copySelectedFiles(itemAdapter,selectExtension,selectedFolderPath);
                    }).show(getSupportFragmentManager(), "FolderPickerDialog");

                    selectionBarVisibility(false);
                }
            }
        });
        moveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!selectExtension.getSelections().isEmpty()){
                    new FolderPickerDialog(selectedFolderPath -> {
                        fm.moveSelectedFiles(itemAdapter,selectExtension,selectedFolderPath);
                    }).show(getSupportFragmentManager(), "FolderPickerDialog");

                    selectionBarVisibility(false);
                }
            }
        });

        sortingAccDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortingOrder = !sortingOrder;
                editor.putBoolean("sortingOrder",sortingOrder);
                sm.sortBy(itemAdapter,fastAdapter, itemList,sortingType,sortingOrder);
                if(sortingOrder){
                    sortingAccDesc.setImageResource(R.drawable.arrow_up_short_ic);
                }else{
                    sortingAccDesc.setImageResource(R.drawable.arrow_down_sort_ic);
                }
                editor.apply();
            }
        });
        sortingTypeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu menu = new PopupMenu(FileManagerActivity.this,view );
                menu.getMenuInflater().inflate(R.menu.sorting_menu,menu.getMenu());
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        if(id == R.id.nameBtn){
                            sortingType = SortingType.NAME.name();
                        }else if(id ==  R.id.dateBtn){
                            sortingType = SortingType.DATE.name();
                        }else if(id ==  R.id.sizeBtn){
                            sortingType = SortingType.SIZE.name();
                        }else{
                            return false;
                        }
                        sm.sortBy(itemAdapter,fastAdapter, itemList,sortingType,sortingOrder);
                        editor.putString("sortingType",sortingType);
                        sortingTypeName.setText(sortingType);
                        editor.apply();
                        return true;
                    }
                });
                menu.show();
            }
        });
    }

    private void selectionBarVisibility(boolean b) {
        if(b){
            selectionBar.setVisibility(View.VISIBLE);
            selectionBottomBar.setVisibility(View.VISIBLE);
        }else{
            selectExtension.deselect();
            selectionBar.setVisibility(View.GONE);
            selectionBottomBar.setVisibility(View.GONE);
        }
    }

    private void fastadapterListener() {
        fastAdapter.setOnLongClickListener((v, adapter, item, position) -> {
            if (item != null) {
                applySelection(position);
                return true;
            }
            return false;
        });

        fastAdapter.setOnClickListener((v, adapter, item, position) -> {
            if (!selectExtension.getSelections().isEmpty()) {
                applySelection(position);
            }else{
                File file = item.getFile();
                Log.d("File:",file.getName());

                if (file.isDirectory()) {
                    loadFiles(file);
                } else {
                    fm.openFile(this,file);
                }

            }
            return true;
        });

        fastAdapter.addEventHook(new ClickEventHook<FileItem>() {

            @Nullable
            @Override
            public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                if(viewHolder instanceof FileItem.ViewHolder){
                    return ((FileItem.ViewHolder)viewHolder).optionBtn;
                }
                return null;
            }

            @Override
            public void onClick(@NonNull View view, int i, @NonNull FastAdapter<FileItem> fastAdapter, @NonNull FileItem item) {
                PopupMenu popup = new PopupMenu(FileManagerActivity.this, view);

                popup.getMenuInflater().inflate(R.menu.file_option_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem it) {
                        int itemId = it.getItemId();

                        if (itemId == R.id.selectBtn) {
                            applySelection(i);
                            return true;
                        } else if (itemId == R.id.copyBtn) {
                            new FolderPickerDialog(new FolderPickerDialog.OnFolderSelectedListener() {
                                @Override
                                public void onFolderSelected(String folderPath) {
                                    fm.copyFileOrFolder(item.getFile().getAbsolutePath(),folderPath);
                                }
                            }).show(getSupportFragmentManager(),"Select Folder ");

                            return true;
                        } else if (itemId == R.id.moveBtn) {
                            new FolderPickerDialog(new FolderPickerDialog.OnFolderSelectedListener() {
                                @Override
                                public void onFolderSelected(String folderPath) {
                                    fm.moveFileOrFolder(itemAdapter,i,item.getFile().getAbsolutePath(),folderPath);
                                    itemAdapter.remove(i);
                                    fastAdapter.notifyAdapterItemRemoved(i);
                                }
                            }).show(getSupportFragmentManager(),"Select Folder ");

                            return true;
                        } else if (itemId == R.id.renameBtn) {
                            fm.showAlertDialog(FileManagerActivity.this, "Rename File/Folder", null, "Rename", "Cancel", ADD_FOLDER, FileManager.NEW_FOLDER, new LoadAlertDialogBox() {
                                @Override
                                public void onLoadAlertDialog(String newName) {
                                    fm.renameFolderOrFile(item.getFile(),newName);
                                    fastAdapter.notifyDataSetChanged();
                                    selectionBarVisibility(false);
                                }
                            });
                            return true;
                        } else if (itemId == R.id.deleteBtn) {
                            fm.showAlertDialog(FileManagerActivity.this, "Delete File(s)/Folder(s)", "Are you sure you want to delete "+item.getFile().getName(), "OK", "Cancel", ActionType.DELETE, item.getFile().getName(), new LoadAlertDialogBox() {
                                @Override
                                public void onLoadAlertDialog(String newName) {
                                    fm.deleteFileOrFolder(item.getFile());
                                    itemAdapter.remove(i);
                                    fastAdapter.notifyAdapterItemRemoved(i);

                                }
                            });
                            return true;
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });
    }

    private void updateBreadcrumbs(File directory) {
        breadcrumbs.clear();
        File temp = directory;

        while (temp != null && temp.exists()) {
            breadcrumbs.add(0, new BreadcrumbItem(temp)); // Add to front
            temp = temp.getParentFile();
        }
        breadcrumbItemAdapter.set(breadcrumbs.subList(4,breadcrumbs.size()));
    }

    private void initiateUI() {
        breadcrumbs = new ArrayList<>();

        breadcrumbRecyclerView = findViewById(R.id.breadcrumbRecyclerView);

        breadcrumbRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));


        breadcrumbItemAdapter = new ItemAdapter<>();
        breadcrumbFastAdapter = FastAdapter.with(breadcrumbItemAdapter);
        breadcrumbRecyclerView.setAdapter(breadcrumbFastAdapter);


        addFolderBtn = findViewById(R.id.addFolderBtn);

        allFileList = new ArrayList<>();

        itemList = new ArrayList<>();

        sharedPreferences = getSharedPreferences(SORTING_PREF,MODE_PRIVATE);
        editor = sharedPreferences.edit();


        sortingOrder = sharedPreferences.getBoolean(SORTING_ORDER,true);
        sortingType = sharedPreferences.getString(SORTING_TYPE,"Name");

        selectionBar = findViewById(R.id.selectionBar);
        selectionBottomBar = findViewById(R.id.selectionBottomBar);
        selectedCount = findViewById(R.id.selectedCount);
        copyBtn = findViewById(R.id.copyBtn);
        renameBtn = findViewById(R.id.renameBtn);
        moveBtn = findViewById(R.id.moveBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        noFiles = findViewById(R.id.noFiles);
        progressBar = findViewById(R.id.progress);

        sortingTypeName = findViewById(R.id.sortingTypeName);
        homeBtn = findViewById(R.id.homeBtn);

        sortingTypeLayout = findViewById(R.id.sortingType);
        sortingAccDesc = findViewById(R.id.sortingAccDesc);


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(itemAdapter);

        recyclerView.setAdapter(fastAdapter);

        internalStorage = Environment.getExternalStorageDirectory();

        selectExtension = new SelectExtension<>(fastAdapter);
        selectExtension.setSelectable(true);
        selectExtension.setMultiSelect(true);

        fm = new FileManager();
        sm = new SortingManager();
        bt = new BackgroundThread();

        Intent in = getIntent();
        String st = in.getStringExtra("Storage");
        if(st!=null)
            storage = new File(st);

        fileCategory = in.getStringExtra("FILE_CATEGORY");

        if (fileCategory != null) {
            loadFilesByCategory(fileCategory);
        }else {
            loadFiles(storage);
            bt.loadAllFiles(allFileList,currentDirectory);
        }

        sortingTypeName.setText(sortingType);
    }
    private void applySelection(int position){
        int count = selectExtension.getSelections().size();
        if(count == 0){
            selectionBarVisibility(false);
        }else if(count == 1){
            renameBtn.setVisibility(View.VISIBLE);
        }else{
            renameBtn.setVisibility(View.GONE);
        }
        selectionBarVisibility(true);

//        selectExtension.toggleSelection(position);
        fastAdapter.notifyItemChanged(position);
        selectedCount.setText(String.valueOf(count+" Selected"));

    }


    private void loadFilesByCategory(String category) {
        new LoadFilesTaskByCategory(category, this, itemAdapter, fastAdapter, noFiles, progressBar, new LoadFilteredList() {
            @Override
            public void onLoadFilteredList(List<FileItem> list) {
                itemList = new ArrayList<>(list);
                allFileList = new ArrayList<>(itemList);
            }
        }).load();
    }

    private void loadFiles(File directory) {
        new LoadFilesFolders(directory, this, itemAdapter, fastAdapter, noFiles, progressBar, new LoadFilteredList() {
            @Override
            public void onLoadFilteredList(List<FileItem> list) {
                itemList = new ArrayList<>(list);
            }
        }).load();
        currentDirectory = directory;
        updateBreadcrumbs(directory);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return search(query);
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return search(newText);
            }

        });

        return true;
    }

    private boolean search(String query) {
        if(!query.isEmpty()) {
            bt.searchFiles(allFileList, itemAdapter, fastAdapter, query, new LoadSearchList() {
                @Override
                public void onLoadSearchList(List<FileItem> list) {
                    if(list.isEmpty()){
                        Toast.makeText(FileManagerActivity.this,"No Item(s) Found",Toast.LENGTH_SHORT).show();
                    }else {
                        itemAdapter.clear();
                        itemAdapter.set(list);
                        fastAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
        else {
            fm.refreshList(itemList, itemAdapter, fastAdapter);
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        if(!selectExtension.getSelections().isEmpty()){

            selectionBarVisibility(false);
        }else if(currentDirectory != null){
            if(currentDirectory.getAbsolutePath().equals(internalStorage.getAbsolutePath())){
                super.onBackPressed();
            }else {
                File parentDir = currentDirectory.getParentFile();
                Log.e("Path check: ", parentDir + " == " + internalStorage.getAbsolutePath());

                if (parentDir != null) {
                    loadFiles(parentDir);
                } else {
                    super.onBackPressed();
                }

            }
        }else{
            super.onBackPressed();
        }


    }

}