package com.lufick.files.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.lufick.files.R;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import java.io.File;
import java.util.List;

public class BreadcrumbItem extends AbstractItem<BreadcrumbItem.ViewHolder> {
    private final File directory;

    public BreadcrumbItem(File directory) {
        this.directory = directory;
    }

    public File getDirectory() {
        return directory;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.fastadapter_breadcrumb_item_id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_breadcrumb;
    }

    public static class ViewHolder extends FastAdapter.ViewHolder<BreadcrumbItem> {
        public TextView breadcrumbText;

        ViewHolder(View itemView) {
            super(itemView);
            breadcrumbText = itemView.findViewById(R.id.breadcrumbText);
        }

        @Override
        public void bindView(BreadcrumbItem item, @NonNull List<?> payloads) {
            breadcrumbText.setText(item.getDirectory().getName());
        }

        @Override
        public void unbindView(BreadcrumbItem item) {
            breadcrumbText.setText(null);
        }
    }
}

