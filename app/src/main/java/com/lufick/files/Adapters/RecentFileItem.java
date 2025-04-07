package com.lufick.files.Adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.lufick.files.Controls.FileManager;
import com.lufick.files.R;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.io.File;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentFileItem extends AbstractItem< RecentFileItem.ViewHolder> {

    private File file;

    public RecentFileItem(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.recent_file_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.recent_item;
    }

    public class ViewHolder extends FastAdapter.ViewHolder<RecentFileItem> {

        ImageView icon;
        TextView title, time;

        public ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.file_icon);
            title = itemView.findViewById(R.id.file_title);
            time = itemView.findViewById(R.id.modifiedTime);
        }

        public String getFormattedDate() {
            long lastModified = file.lastModified();
            long currentTime = System.currentTimeMillis();
            long diff = currentTime - lastModified;

            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (seconds < 60) {
                return "Now";
            } else if (minutes < 60) {
                return minutes + " min ago";
            } else if (hours < 24) {
                return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
            } else if (days == 1) {
                return "Yesterday";
            } else if (days < 7) {
                return days + " days ago";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                return sdf.format(new Date(lastModified));
            }
        }

        @Override
        public void bindView(RecentFileItem item, List<?> payloads) {

            title.setText(item.getFile().getName());

            time.setText(getFormattedDate());


            FileManager fm = new FileManager();

            String mimeType = fm.getMimeType(item.getFile());
            if (mimeType == null) {
                mimeType = URLConnection.guessContentTypeFromName(item.getFile().getName());
            }

            fm.setFileIcon(icon,itemView,mimeType,item.getFile());


        }

        @Override
        public void unbindView(RecentFileItem item) {
            icon.setImageDrawable(null);
            title.setText(null);
        }
    }
}
