package com.lufick.files.Callbacks;

import com.lufick.files.Adapters.FileItem;

import java.util.List;

public interface LoadFileList {
    void onLoad(List<FileItem> filteredList);
}
