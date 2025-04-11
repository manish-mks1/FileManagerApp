package com.lufick.files.Callbacks;

import com.lufick.files.Adapters.FileItem;
import com.lufick.files.Adapters.RecentFileItem;

import java.util.List;

public interface LoadRecentList {
    void onLoadRecentList(List<RecentFileItem> recentList);
}
