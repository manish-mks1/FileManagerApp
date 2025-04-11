package com.lufick.files.Controls;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lufick.files.Adapters.QuickAccessItem;
import com.lufick.files.Adapters.RecentFileItem;
import com.lufick.files.Adapters.StorageDeviceItem;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

public class ListManager {
    public <T extends AbstractItem<?>> void loadItem(Context context, List<T> items, RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        ItemAdapter<T> itemAdapter = new ItemAdapter<>();
        FastAdapter<T> fastAdapter = FastAdapter.with(itemAdapter);
        itemAdapter.add(items);
        recyclerView.setAdapter(fastAdapter);
    }

    public void loadQuickAccessItem(Context context, List<QuickAccessItem> items, RecyclerView recyclerView){
        recyclerView.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false));
        ItemAdapter<QuickAccessItem> itemAdapter = new ItemAdapter<>();
        FastAdapter<QuickAccessItem> fastAdapter = FastAdapter.with(itemAdapter);
        itemAdapter.add(items);
        recyclerView.setAdapter(fastAdapter);
    }
    public void loadStorageItem(Context context, List<StorageDeviceItem> items, RecyclerView recyclerView){
        recyclerView.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false));
        ItemAdapter<StorageDeviceItem> itemAdapter = new ItemAdapter<>();
        FastAdapter<StorageDeviceItem> fastAdapter = FastAdapter.with(itemAdapter);
        itemAdapter.add(items);
        recyclerView.setAdapter(fastAdapter);
    }

}
