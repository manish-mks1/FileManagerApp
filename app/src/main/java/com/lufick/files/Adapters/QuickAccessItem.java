package com.lufick.files.Adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lufick.files.R;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

public class QuickAccessItem extends AbstractItem< QuickAccessItem.ViewHolder> {

    private String title;
    private int iconRes;
    private String storageInfo;

    public QuickAccessItem(String title, String storageInfo, int iconRes) {
        this.title = title;
        this.iconRes = iconRes;
        this.storageInfo = storageInfo;
    }

    public String getStorageInfo() {
        return storageInfo;
    }

    public void setStorageInfo(String storageInfo) {
        this.storageInfo = storageInfo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.quick_access_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_quick_access;
    }

    public class ViewHolder extends FastAdapter.ViewHolder<QuickAccessItem> {

        ImageView icon;
        TextView title;
        TextView storage_info;

        public ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.quick_access_icon);
            title = itemView.findViewById(R.id.quick_access_title);
            storage_info = itemView.findViewById(R.id.storage_info);
        }

        @Override
        public void bindView(QuickAccessItem item, List<?> payloads) {
            icon.setImageResource(item.iconRes);
            title.setText(item.title);
            storage_info.setText(item.getStorageInfo());

        }

        @Override
        public void unbindView(QuickAccessItem item) {
            icon.setImageDrawable(null);
            title.setText(null);
        }
    }
}
