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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SortingManager {
    ExecutorService executorService = Executors.newSingleThreadExecutor();

}
