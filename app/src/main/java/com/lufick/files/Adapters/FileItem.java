package com.lufick.files.Adapters;


import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class FileItem extends AbstractItem< FileItem.ViewHolder>{

    private final File file;

    private String searchQuery = "";

    public FileItem(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }


    public void setSearchQuery(String query) {
        this.searchQuery = query.toLowerCase();
    }

    public Spannable getHighlightedName() {
        String fileName = file.getName();
        Spannable spannable = new SpannableString(fileName);

        if (!searchQuery.isEmpty()) {
            int start = fileName.toLowerCase().indexOf(searchQuery);
            if (start >= 0) {
                int end = start + searchQuery.length();
                spannable.setSpan(new ForegroundColorSpan(Color.GRAY), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return spannable;
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(file.lastModified()));
    }


    public int getItemCount() {
        if (file.isDirectory()) {
            File[] items = file.listFiles();
            int count = (items != null) ? items.length : 0;
            return count;
        }
        return 0;
    }

    @Override
    public int getType() {
        return R.id.file_item_layout;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_file;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(@NonNull View view) {
        return new ViewHolder(view);
    }

    public class ViewHolder extends FastAdapter.ViewHolder<FileItem> {

        TextView fileName, fileDateTime, file_count_size;
        ImageView fileIcon;
        public ImageView optionBtn;
        LinearLayout file_item_layout;
        FileManager fm;

        ViewHolder(View view) {
            super(view);
            fileName = view.findViewById(R.id.file_name);
            fileDateTime = view.findViewById(R.id.file_date_time);
            file_count_size = view.findViewById(R.id.file_count_size);
            fileIcon = view.findViewById(R.id.file_icon);
            file_item_layout = view.findViewById(R.id.file_item_layout);
            optionBtn = view.findViewById(R.id.optionBtn);
        }

        public void bindView(FileItem item, List<?> payloads) {
            if (item.isSelected()) {
                file_item_layout.setBackgroundResource(R.drawable.background_rounded); // Light Blue when selected
            } else {
                file_item_layout.setBackgroundColor(Color.TRANSPARENT);
            }

            fm = new FileManager();


            fileName.setText(item.getHighlightedName());
            fileDateTime.setText(item.getFormattedDate());

            if (item.getFile().isDirectory()) {
                file_count_size.setText(item.getItemCount()+" items");
                if(item.getItemCount()>0)
                    fileIcon.setImageResource(R.drawable.folder_icon);
                else
                    fileIcon.setImageResource(R.drawable.folder_empty);
            } else {
                file_count_size.setText(fm.formatFileSize(item.getFile().length()));

                String mimeType = fm.getMimeType(item.getFile());
                if (mimeType == null) {
                    mimeType = URLConnection.guessContentTypeFromName(item.getFile().getName());
                }

                fm.setFileIcon(fileIcon,itemView,mimeType,item.getFile());
            }

        }


        @Override
        public void unbindView(FileItem item) {
            fileName.setText(null);
            fileDateTime.setText(null);
            file_count_size.setText(null);
        }
    }
}


