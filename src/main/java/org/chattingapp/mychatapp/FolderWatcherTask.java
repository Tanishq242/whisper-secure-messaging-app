package org.chattingapp.mychatapp;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class FolderWatcherTask implements Runnable {
    private Path folder;
    HBox root;
    VBox importMid;
    VBox vboxRight2;
    Label vboxRight2Label;
    private mainCode app;

    public FolderWatcherTask(mainCode app, Path folder, HBox root, VBox importMid, VBox vboxRight2, Label vboxRight2Label) {
        this.app = app;
        this.folder = folder;
        this.root = root;
        this.importMid = importMid;
        this.vboxRight2 = vboxRight2;
        this.vboxRight2Label = vboxRight2Label;
    }

    @Override
    public void run() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            folder.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);

            Map<String, Long> debounceMap = new HashMap<>();

            System.out.println("Watching for JSON files in: " + folder.toAbsolutePath());

            while (true) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    String fileName = event.context().toString();
                    String lower = fileName.toLowerCase();

                    // ✅ Only handle .json files
                    if (!lower.endsWith(".json"))
                        continue;

                    long now = System.currentTimeMillis();
                    Long last = debounceMap.get(lower);

                    if (last == null || now - last > 1500) {
                        debounceMap.put(lower, now);

                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            File file = new File(folder.toFile(), fileName);
                            if (file.isFile())
                                waitUntilStable(file);
                            Platform.runLater(() -> app.importChatFiles(file, fileName, root, importMid, vboxRight2, vboxRight2Label));
                        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            Platform.runLater(() -> app.removeImportChatFiles(importMid, fileName, vboxRight2));
                        }
                    }
                }

                if (!key.reset())
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void waitUntilStable(File file) {
        try {
            long lastSize = -1;
            int stableCount = 0;
            while (stableCount < 3) {
                long size = file.length();
                if (size == lastSize) {
                    stableCount++;
                } else {
                    lastSize = size;
                    stableCount = 0;
                }
                Thread.sleep(200);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}