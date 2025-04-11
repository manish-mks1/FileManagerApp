package com.lufick.files.Adapters;

import static com.lufick.files.Controls.FileManager.getExternalSDCardPath;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lufick.files.Controls.FileManager;
import com.lufick.files.Controls.ListManager;
import com.lufick.files.Enumeration.CategoryType;
import com.lufick.files.FileManagerActivity;
import com.lufick.files.R;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.io.File;
import java.util.List;

import kotlin.jvm.functions.Function4;

public class MainItemAdapter<T extends AbstractItem> extends AbstractItem<MainItemAdapter.ViewHolder> {
    Context context;
    String categoryType;
    List<T> items; // Generic list of type T

    public MainItemAdapter(Context context, String categoryType, List<T> items) {
        this.context = context;
        this.categoryType = categoryType;
        this.items = items;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public void setType(String categoryType) {
        this.categoryType = categoryType;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.main_activity_item;
    }

    @Override
    public int getType() {
        return R.id.main_activity_item_layout;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(@NonNull View view) {
        return new ViewHolder(view);
    }

    public class ViewHolder extends FastAdapter.ViewHolder<MainItemAdapter<T>> { // Make ViewHolder also generic
        TextView categoryType;
        RecyclerView recyclerView;
        Context context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryType = itemView.findViewById(R.id.categoryType);
            recyclerView = itemView.findViewById(R.id.recycleView);
            context = itemView.getContext();
        }

        @Override
        public void bindView(@NonNull MainItemAdapter<T> item, @NonNull List<?> payloads) {
            String type = item.getCategoryType();
            if(type!=null){
                ListManager lm = new ListManager();
                categoryType.setText(type);
                if(type.equals(CategoryType.Recent.name())){
                    loadItem(item.getItems());
                }
            }

        }
        public <T extends AbstractItem<?>> void loadItem( List<T> items) {
            if (items == null || items.isEmpty()) return;
            T firstItem = items.get(0);
            if (firstItem instanceof RecentFileItem) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            } else if (firstItem instanceof QuickAccessItem) {
                recyclerView.setLayoutManager(new GridLayoutManager(context, 3));
            } else if (firstItem instanceof StorageDeviceItem) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            }
            ItemAdapter<T> itemAdapter = new ItemAdapter<>();
            FastAdapter<T> fastAdapter = FastAdapter.with(itemAdapter);
            itemAdapter.add(items);
            recyclerView.setAdapter(fastAdapter);


            fastAdapter.setOnClickListener(new Function4<View, IAdapter<T>, T, Integer, Boolean>() {
                @Override
                public Boolean invoke(View view, IAdapter<T> tiAdapter, T t, Integer integer) {
                    if (t instanceof RecentFileItem) {
                        RecentFileItem item = (RecentFileItem) t;
                        File file = item.getFile();
                        new FileManager().openFile(context, file);
                    }else if(t instanceof QuickAccessItem) {
                        QuickAccessItem item = (QuickAccessItem) t;
                        openFileCategory(item.getTitle());
                    }else if(t instanceof StorageDeviceItem){
                        StorageDeviceItem item = (StorageDeviceItem) t;
                        openFileStorage(item);
                    }
                    return true;
                }
            });
        }

        private void openFileStorage(StorageDeviceItem item){
            Intent intent = new Intent(context,FileManagerActivity.class);
            String internalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            if(item.deviceName.equals("Internal Storage")){
                intent.putExtra("Storage",internalStoragePath);
                context.startActivity(intent);
            }else if(item.deviceName.equals("SD Card")){
                File sdCard = getExternalSDCardPath(context);
                if (sdCard != null && sdCard.isDirectory()) {
                    intent.putExtra("Storage",sdCard.getAbsolutePath());
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "SD Card not found", Toast.LENGTH_SHORT).show();
                }

            }
        }

        private void openFileCategory(String category) {
            Intent intent = new Intent(context, FileManagerActivity.class);
            if(category.equals("Downloads"))
                intent.putExtra("Storage", Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download");
            else
                intent.putExtra("FILE_CATEGORY", category);
            context.startActivity(intent);
        }


        @Override
        public void unbindView(@NonNull MainItemAdapter<T> item) {
            categoryType.setText(null);
        }
    }
}
