package org.chattingapp.mychatapp;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;

enum FileType {
    DOCUMENT(Arrays.asList("doc", "docx", "pdf", "txt", "xls", "xlsx", "ppt", "pptx")),
    IMAGE(Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "svg", "webp")),
    MUSIC(Arrays.asList("mp3", "wav", "flac", "aac", "ogg", "m4a")),
    VIDEO(Arrays.asList("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm")),
    UNKNOWN(Collections.emptyList()); // Fixed: Use Collections.emptyList() instead of Arrays.asList()

    private final List<String> extensions;

    FileType(List<String> extensions) {
        this.extensions = extensions;
    }

    public static FileType fromFileName(String fileName) {
        // Fixed: Added null and empty checks, and check for '.' existence
        if (fileName == null || fileName.isEmpty()) {
            return UNKNOWN;
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return UNKNOWN; // No extension found
        }

        String ext = fileName.substring(lastDotIndex + 1).toLowerCase();

        for (FileType type : values()) {
            if (type.extensions.contains(ext)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}