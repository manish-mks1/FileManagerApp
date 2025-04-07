package com.lufick.files.Sorting;

import com.lufick.files.Adapters.FileItem;
import com.lufick.files.Controls.FileManager;

import java.util.Comparator;

public class SortBySize implements Comparator<FileItem> {
    private final boolean order;

    public SortBySize(boolean order) {
        this.order = order;
    }

    @Override
    public int compare(FileItem fileItem, FileItem t1) {
        if(fileItem.getFile().isDirectory() && !t1.getFile().isDirectory()){
            return -1;
        }else if (!fileItem.getFile().isDirectory() && t1.getFile().isDirectory()){
            return 1;
        }
        int result = Long.compare(fileItem.getFile().length(), t1.getFile().length());
        return order ? result : -result;
    }

}
