package com.lufick.files;

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
import com.lufick.files.Callbacks.LoadFilteredList;
import com.lufick.files.Callbacks.LoadSearchList;
import com.lufick.files.Adapters.FileItem;
import com.lufick.files.AsyncTask.LoadFilesFolders;
import com.lufick.files.AsyncTask.LoadFilesTaskByCategory;
import com.lufick.files.BackgroundTask.BackgroundThread;
import com.lufick.files.Controls.FileManager;
import com.lufick.files.Controls.SortingManager;
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

    public static final String file_category_key = "file_category_key";
    File currentDirectory;
    File internalStorage;
    SelectExtension<FileItem> selectExtension;
    boolean isFirstSelectionMade;
    private RecyclerView recyclerView, breadcrumbRecyclerView;
    File storage ;
    String fileCategory;
    List<BreadcrumbItem> breadcrumbs;

    private FastAdapter<BreadcrumbItem> breadcrumbFastAdapter;
    private ItemAdapter<BreadcrumbItem> breadcrumbItemAdapter;

    List<FileItem> filteredList;
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
            AlertDialog.Builder builder = new AlertDialog.Builder(FileManagerActivity.this);
            final EditText input = new EditText(FileManagerActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setTitle("Create Folder");
            input.setHint("Folder Name");
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(50, 30, 50, 10); // left, top, right, bottom margin
            input.setLayoutParams(layoutParams);
            builder.setPositiveButton("Create", (dialog, which) -> {
                String newName = input.getText().toString();
                if(!newName.isEmpty()) {
                    fm.addFolder(view,currentDirectory,newName);
                    loadFiles(currentDirectory);
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
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
            AlertDialog.Builder builder = new AlertDialog.Builder(FileManagerActivity.this);
            builder.setTitle("Delete File(s)/Folder(s)");
            builder.setMessage("Are you sure you want to delete this item?");

            builder.setPositiveButton("OK", (dialog, which) -> {

                fm.deleteSelectedFiles(itemAdapter,selectExtension);

                fastAdapter.notifyDataSetChanged();

                selectionBar.setVisibility(View.GONE);
                selectionBottomBar.setVisibility(View.GONE);
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();
        });




        renameBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(FileManagerActivity.this);

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setTitle("Rename File/Folder");
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(50, 30, 50, 10); // left, top, right, bottom margin
            input.setLayoutParams(layoutParams);

            builder.setPositiveButton("Rename", (dialog, which) -> {

                String newName = input.getText().toString();
                if(!newName.isEmpty()) {
                    fm.renameSelectedFolderFiles(itemAdapter,fastAdapter,selectExtension,newName);
                }
                isFirstSelectionMade = false;

                fastAdapter.notifyDataSetChanged();

                selectionBar.setVisibility(View.GONE);
                selectionBottomBar.setVisibility(View.GONE);
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();
        });




        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!selectExtension.getSelections().isEmpty()){
                    new FolderPickerDialog(selectedFolderPath -> {
                        fm.copySelectedFiles(itemAdapter,selectExtension,selectedFolderPath);
                    }).show(getSupportFragmentManager(), "FolderPickerDialog");

                    selectionBar.setVisibility(View.GONE);
                    selectionBottomBar.setVisibility(View.GONE);
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

                    selectionBar.setVisibility(View.GONE);
                    selectionBottomBar.setVisibility(View.GONE);
                }
            }
        });



        sortingAccDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortingOrder = !sortingOrder;
                editor.putBoolean("sortingOrder",sortingOrder);
                sm.sortBy(itemAdapter,fastAdapter,filteredList,sortingType,sortingOrder);
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
                        sm.sortBy(itemAdapter,fastAdapter,filteredList,sortingType,sortingOrder);
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

    private void fastadapterListener() {

        fastAdapter.setOnLongClickListener((v, adapter, item, position) -> {
            if (!isFirstSelectionMade && item != null) {
                applyFirstSelection(position);
                return true;
            }
            return false;
        });


        fastAdapter.setOnClickListener((v, adapter, item, position) -> {
            if (isFirstSelectionMade && item != null) {
                applyMultiSelection(position);
                return true;
            }else{
                File file = item.getFile();
                Log.d("File:",file.getName());

                if (file.isDirectory()) {
                    loadFiles(file);
                } else {
                    fm.openFile(this,file);
                }

            }
            return false;
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
                            applyFirstSelection(i);
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(FileManagerActivity.this);

                            final EditText input = new EditText(FileManagerActivity.this);
                            input.setInputType(InputType.TYPE_CLASS_TEXT);
                            builder.setView(input);
                            builder.setTitle("Rename File/Folder");
                            input.setText(item.getFile().getName());
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            layoutParams.setMargins(50, 30, 50, 10); // left, top, right, bottom margin
                            input.setLayoutParams(layoutParams);

                            builder.setPositiveButton("Rename", (dialog, which) -> {

                                String newName = input.getText().toString();
                                if(!newName.isEmpty()) {
                                    fm.renameFolderOrFile(item.getFile(),newName);
                                }
                                fastAdapter.notifyItemChanged(i);

                            });

                            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return true;
                        } else if (itemId == R.id.deleteBtn) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(FileManagerActivity.this);
                            builder.setTitle("Delete File(s)/Folder(s)");
                            builder.setMessage("Are you sure you want to delete this item?");

                            builder.setPositiveButton("OK", (dialog, which) -> {
                                fm.deleteFileOrFolder(item.getFile());
                                itemAdapter.remove(i);
                                fastAdapter.notifyAdapterItemRemoved(i);
                            });
                            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                            AlertDialog dialog = builder.create();
                            dialog.show();

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

        filteredList = new ArrayList<>();

        sharedPreferences = getSharedPreferences("Files_sorting_pref",MODE_PRIVATE);
        editor = sharedPreferences.edit();


        sortingOrder = sharedPreferences.getBoolean("sortingOrder",true);
        sortingType = sharedPreferences.getString("sortingType","Name");

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


        selectionBar.setVisibility(View.GONE);
        selectionBottomBar.setVisibility(View.GONE);

        sortingTypeName.setText(sortingType);
    }
    private void applyFirstSelection(int position){
        renameBtn.setVisibility(View.VISIBLE);
        selectExtension.toggleSelection(position);
        fastAdapter.notifyItemChanged(position);
        count = 1;
        selectedCount.setText(String.valueOf(count+" Selected"));
        isFirstSelectionMade = true;
        selectionBar.setVisibility(View.VISIBLE);
        selectionBottomBar.setVisibility(View.VISIBLE);
    }
    private void applyMultiSelection(int position) {
        renameBtn.setVisibility(View.GONE);
        selectExtension.toggleSelection(position);
        count = selectExtension.getSelections().size();
        selectedCount.setText(String.valueOf(count+" Selected"));
        fastAdapter.notifyItemChanged(position);

        if (selectExtension.getSelections().isEmpty()) {
            isFirstSelectionMade = false;
            count = 0;
            selectionBar.setVisibility(View.GONE);
            selectionBottomBar.setVisibility(View.GONE);
        }
        if(count<2){
            renameBtn.setVisibility(View.VISIBLE);
        }else{
            renameBtn.setVisibility(View.GONE);
        }
    }


    private void loadFilesByCategory(String category) {
        new LoadFilesTaskByCategory(category, this, itemAdapter, fastAdapter, noFiles, progressBar, new LoadFilteredList() {
            @Override
            public void onLoadFilteredList(List<FileItem> list) {
                filteredList = new ArrayList<>(list);
                allFileList = new ArrayList<>(filteredList);
            }
        }).execute();
    }

    private void loadFiles(File directory) {
        new LoadFilesFolders(directory, this, itemAdapter, fastAdapter, noFiles, progressBar, new LoadFilteredList() {
            @Override
            public void onLoadFilteredList(List<FileItem> list) {
                filteredList = new ArrayList<>(list);
            }
        }).execute();
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
            fm.refreshList(filteredList, itemAdapter, fastAdapter);
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        if(!selectExtension.getSelections().isEmpty()){
            selectExtension.deselect();
            isFirstSelectionMade = false;
            selectionBar.setVisibility(View.GONE);
            selectionBottomBar.setVisibility(View.GONE);
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