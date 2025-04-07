package com.lufick.files.Adapters;

import static com.lufick.files.Controls.FileManager.getExternalSDCardPath;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.lufick.files.Controls.FileManager;
import com.lufick.files.FileManagerActivity;
import com.lufick.files.R;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.io.File;
import java.util.List;

public class StorageDeviceItem extends AbstractItem<StorageDeviceItem.ViewHolder> {

    public String deviceName;
    public String storageInfo;
    public int icon;

    public StorageDeviceItem(int icon, String deviceName, String storageInfo) {
        this.deviceName = deviceName;
        this.storageInfo = storageInfo;
        this.icon = icon;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.storage_device_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_storage_device;
    }

    public class ViewHolder extends FastAdapter.ViewHolder<StorageDeviceItem> {

        TextView deviceNameText, storageInfoText;
        ImageView icon;
        LinearLayout storageLayout;


        public ViewHolder(View itemView) {
            super(itemView);
            deviceNameText = itemView.findViewById(R.id.device_name);
            storageInfoText = itemView.findViewById(R.id.storage_info);
            icon = itemView.findViewById(R.id.icon);
            storageLayout = itemView.findViewById(R.id.storage_device_item);

        }

        @Override
        public void bindView(StorageDeviceItem item, List<?> payloads) {
            deviceNameText.setText(item.deviceName);
            storageInfoText.setText(item.storageInfo);
            icon.setImageResource(item.icon);

        }

        @Override
        public void unbindView(StorageDeviceItem item) {
            deviceNameText.setText(null);
            storageInfoText.setText(null);

        }


    }
}

