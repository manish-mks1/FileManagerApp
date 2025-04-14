package com.lufick.files.Enumeration;

public enum FileCategory {
    IMAGES(null),
    VIDEOS(null),
    AUDIO(null),
    DOCUMENTS(new String[]{
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain"
    }),
    APK(new String[]{"application/vnd.android.package-archive"}),
    DOWNLOADS(null);

    private final String[] mimeTypes;

    FileCategory(String[] mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public String[] getMimeTypes() {
        return mimeTypes;
    }
}
