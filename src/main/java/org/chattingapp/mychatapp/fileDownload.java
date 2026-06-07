package org.chattingapp.mychatapp;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class fileDownload {
    static Map<String, ProgressBar> progressBarMap = new HashMap<>();
    static double temp = 0;
    static mainCode app;
    static msgEncodeDecode msg_encode = new msgEncodeDecode();
    static readJsonFile readJson = new readJsonFile();

    public static void receiveFull(String fileId, String fileName, ProgressBar pbar, String mobile, VBox pbarbox) {
        try {
            Socket s = new Socket("localhost", 12346);
            System.out.println("✅ Connected to File Server in receiveFull function.");
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            dos.writeUTF(fileId);
            dos.writeUTF("NULL");
            dos.flush();

            DataInputStream dis = new DataInputStream(s.getInputStream());
            String msg = dis.readUTF();
            long fileSize = dis.readLong();
            if (msg.equals("Ready to Send File")) {
                int iteration = (int) (fileSize / 4096);
                double unitValue = (double) 1 / iteration;
                String filePath = "F:/Programs/My Projects/myChatApp/src/main/resources/encrypted files/" + fileName;
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    byte[] buffer = new byte[4096];
                    int read;
                    long totalRead = 0;
                    while (totalRead < fileSize &&
                            (read = dis.read(buffer, 0,
                                    (int) Math.min(buffer.length, fileSize - totalRead))) > 0) {
                        fos.write(buffer, 0, read);
                        totalRead += read;
                        temp += unitValue;
                        Platform.runLater(() -> pbar.setProgress(temp));
                    }
                    if (temp < 1.0 || temp > 1.0) {
                        Platform.runLater(() -> pbar.setProgress(1));
                    }

                    System.out.println("✅ File received: " + fileName);
                    String file_key = readJson.getFileKey(mobile, fileId.trim());
                    app.progressBarRemover(pbar, pbarbox, fileName, file_key);
                    dis.close();
                    dos.close();
                    s.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("NOT WORKING PROPERLY");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void receiveSplit(String fileId, String extraCode) {
        try {
            Socket s = new Socket("localhost", 12346);
            System.out.println("✅ Connected to File Server.");
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            dos.writeUTF(fileId);
            dos.writeUTF(extraCode);
            dos.flush();

            DataInputStream dis = new DataInputStream(s.getInputStream());
            String msg = dis.readUTF();
            long fileSize = dis.readLong();
            System.out.println("size of file: " + fileSize * 1024 * 1024 + "MB");
            if (msg.equals("Ready to Send File")) {
                int iteration = (int) (fileSize / 4096);
                double unitValue = (double) 1 / iteration;
                String filePath = "F:/Programs/My Projects/myChatApp/splitRecFiles/" + fileId;
                if (extraCode.equals("RP")) filePath = "F:/Programs/My Projects/myChatApp/tempFiles/" + fileId;

                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    byte[] buffer = new byte[4096];
                    int read;
                    long totalRead = 0;
                    while (totalRead < fileSize &&
                            (read = dis.read(buffer, 0,
                                    (int) Math.min(buffer.length, fileSize - totalRead))) > 0) {
                        fos.write(buffer, 0, read);
                        totalRead += read;
                        temp += unitValue;
                        Platform.runLater(() -> progressBarMap.get(fileId).setProgress(temp));
                    }
                    if (temp < 1.0 || temp > 1.0) {
                        Platform.runLater(() -> progressBarMap.get(fileId).setProgress(1));
                    }

                    System.out.println("✅ File received: " + fileId);

                    if (!progressBarMap.get(fileId).isVisible()) {
                        progressBarMap.get(fileId).setVisible(true);
                        progressBarMap.get(fileId).setManaged(true);
                    }

                    if (extraCode.equals("RP") && new File("./splitRecFiles/" + fileId).exists()) {
                        String fileName = ((Label) ((VBox) progressBarMap.get(fileId).getParent()).getChildren().getFirst()).getText();
                        String fileKey = progressBarMap.get(fileId).getParent().getParent().getParent().getId();
                        int mergeStatus = msg_encode.mergeFile(fileId, fileName);

                        if (mergeStatus == 1) {
                            Platform.runLater(() -> app.progressBarRemover(progressBarMap.get(fileId), ((VBox) progressBarMap.get(fileId).getParent()),
                                    fileName, fileKey));
                        }
                    }

                    app.progressBarRemover(progressBarMap.get(fileId));
                    app.w.updateSplitField(progressBarMap.get(fileId).getId(), fileId, true);
                    dis.close();
                    dos.close();
                    s.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void alternateReceiver(String fileId, String extraCode, Label lb) {
        try {
            Socket s = new Socket("localhost", 12346);
            System.out.println("✅ Connected to File Server.");
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            dos.writeUTF(fileId);
            dos.writeUTF(extraCode);
            dos.flush();

            DataInputStream dis = new DataInputStream(s.getInputStream());
            String msg = dis.readUTF();
            long fileSize = dis.readLong();
            System.out.println("size of file: " + fileSize * 1024 * 1024 + "MB");

            String mobileNum = ((HBox) lb.getParent()).getChildren().getFirst().getId();
            String fileName = readJson.searchFileName(Long.parseLong(mobileNum), fileId);
            String filePath = "F:/Programs/My Projects/myChatApp/splitRecFiles/" + fileId;
            if (extraCode.equals("RP")) filePath = "F:/Programs/My Projects/myChatApp/tempFiles/" + fileId;
            if (extraCode.equals("CMPLT")) filePath = "F:/Programs/My Projects/myChatApp/src/main/resources/encrypted files/" + fileName;
            if (msg.equals("Ready to Send File")) {
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    byte[] buffer = new byte[4096];
                    int read;
                    long totalRead = 0;
                    int lastProgress = 0;
                    while (totalRead < fileSize &&
                            (read = dis.read(buffer, 0,
                                    (int) Math.min(buffer.length, fileSize - totalRead))) > 0) {
                        fos.write(buffer, 0, read);
                        totalRead += read;
                        // Calculate current progress percentage
                        int currentProgress = (int) ((totalRead * 100) / fileSize);

                        // Update only when progress changes
                        if (currentProgress > lastProgress) {
                            lastProgress = currentProgress;
                            Platform.runLater(() -> lb.setText(currentProgress + " %"));
                        }
                    }

                    // Ensure it shows 100% at the end
                    Platform.runLater(() -> lb.setText("100 %"));

                    System.out.println("✅ File received: " + fileId);

                    String fileKey = readJson.getFileKey(mobileNum, fileId);
                    System.out.println("FILENAME IS "+fileName);
                    if (extraCode.equals("RP")) {
                        File path1 = new File("F:/Programs/My Projects/myChatApp/splitRecFiles/" + fileId);
                        File path2 = new File("F:/Programs/My Projects/myChatApp/tempFiles/" + fileId);
                        if (path1.exists() && path2.exists()) {
                            Platform.runLater(() -> {
                                ((Hyperlink) ((HBox) lb.getParent()).getChildren().getFirst()).setText("Processing...");
                                lb.setText("merging and decrypting");
                                lb.setPadding(new Insets(0, 0, 0, 50));
                            });

                            int mergeStatus = msg_encode.mergeFile(fileId, fileName);
                            String base64EncodedKey = new String(msg_encode.decryptFileKey(fileKey), StandardCharsets.UTF_8);
                            int decryptFileStatus = msg_encode.decryptFile(Base64.getDecoder().decode(base64EncodedKey), fileName, fileName);

                            if (mergeStatus == 1 && decryptFileStatus == 1) {
                                Platform.runLater(() -> app.completeDownloadUi(fileId));
                            } else {
                                System.out.println("Merge Status is " + mergeStatus);
                                System.out.println("Decrypt Status is " + decryptFileStatus);
                            }
                        }
                    } else if (extraCode.equals("CMPLT")) {
                        String base64EncodedKey = new String(msg_encode.decryptFileKey(fileKey), StandardCharsets.UTF_8);
                        int decryptFileStatus = msg_encode.decryptFile(Base64.getDecoder().decode(base64EncodedKey), fileName, fileName);
                        if (decryptFileStatus == 1) {
                            Platform.runLater(() -> app.downloadUpdate(fileId));
                        } else {
                            System.out.println("Some problem in decrypting the file");
                        }
                    } else if (extraCode.equals("HALF") || extraCode.matches("\\d+")) {

                        Platform.runLater(() -> app.fileDownloadUiChange(fileId.trim()));
                        app.w.updateSplitField(mobileNum, fileId, true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

