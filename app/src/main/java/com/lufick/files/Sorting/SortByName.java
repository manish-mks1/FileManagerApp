package com.lufick.files.Sorting;

import com.lufick.files.Adapters.FileItem;

import java.util.Comparator;

public class SortByName implements Comparator<FileItem> {
    private final boolean order;
    public SortByName(boolean order){
        this.order = order;
    }
    @Override
    public int compare(FileItem fileItem, FileItem t1) {
        if(fileItem.getFile().isDirectory() && !t1.getFile().isDirectory()){
            return -1;
        }else if (!fileItem.getFile().isDirectory() && t1.getFile().isDirectory()){
            return 1;
        }
        int result = fileItem.getFile().getName().toLowerCase().compareTo(t1.getFile().getName().toLowerCase());
        return order ? result : -result;
    }

}
