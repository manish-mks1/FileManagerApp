package com.lufick.files.Sorting;

import com.lufick.files.Adapters.FileItem;

import java.util.Comparator;

public class SortByDate implements Comparator<FileItem> {
    private final boolean order;

    public SortByDate(boolean order) {
        this.order = order;
    }

    @Override
    public int compare(FileItem fileItem, FileItem t1) {

        if(fileItem.getFile().isDirectory() && !t1.getFile().isDirectory()){
            return -1;
        }else if (!fileItem.getFile().isDirectory() && t1.getFile().isDirectory()){
            return 1;
        }
        int result =  Long.compare(fileItem.getFile().lastModified(), t1.getFile().lastModified());
        return order ? result : -result;
    }

}
