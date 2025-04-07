package com.lufick.files.Fragments;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.lufick.files.Adapters.FileItem;
import com.lufick.files.R;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderPickerDialog extends BottomSheetDialogFragment {

    private RecyclerView rvFolders;
    private Button btnPasteHere;
    private String selectedFolderPath;
    private ItemAdapter<FileItem> itemAdapter;
    private FastAdapter<FileItem> fastAdapter;
    private OnFolderSelectedListener listener;

    public interface OnFolderSelectedListener {
        void onFolderSelected(String folderPath);
    }

    public FolderPickerDialog(OnFolderSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_folder_picker, container, false);

        rvFolders = view.findViewById(R.id.rvFolders);
        btnPasteHere = view.findViewById(R.id.btnPasteHere);

        itemAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(itemAdapter);

        rvFolders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFolders.setAdapter(fastAdapter);

        selectedFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        itemAdapter.add(getFolders(Environment.getExternalStorageDirectory()));

        fastAdapter.setOnClickListener((v, adapter, item, position) -> {
            selectedFolderPath = item.getFile().getAbsolutePath();
            itemAdapter.clear();
            itemAdapter.add(getFolders(item.getFile()));
            fastAdapter.notifyDataSetChanged();
            return true;
        });

        btnPasteHere.setOnClickListener(v -> {
            if (listener != null && selectedFolderPath != null) {
                listener.onFolderSelected(selectedFolderPath);
                dismiss();
            }
        });

        return view;
    }

    private List<FileItem> getFolders(File root) {
        List<FileItem> folderItems = new ArrayList<>();
        File[] files = root.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    folderItems.add(new FileItem(file));
                }
            }
        }
        return folderItems;
    }
}
