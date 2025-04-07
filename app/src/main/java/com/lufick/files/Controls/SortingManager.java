package com.lufick.files.Controls;
import com.lufick.files.Adapters.FileItem;
import com.lufick.files.Enumeration.SortingType;
import com.lufick.files.Sorting.SortByDate;
import com.lufick.files.Sorting.SortByName;
import com.lufick.files.Sorting.SortBySize;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class SortingManager {
    public void sortBy(ItemAdapter<FileItem> itemAdapter, FastAdapter<FileItem> fastAdapter,List<FileItem> filteredList, String sortingType, boolean sortingOrder){
        if (sortingType.equals(SortingType.NAME.name()) ) {
            filteredList.sort(new SortByName(sortingOrder));
        } else if (sortingType.equals(SortingType.DATE.name())) {
            filteredList.sort(new SortByDate(sortingOrder));
        } else if (sortingType.equals(SortingType.SIZE.name())) {
            filteredList.sort(new SortBySize(sortingOrder));
        }
        new FileManager().refreshList(filteredList,itemAdapter,fastAdapter);
    }
}
