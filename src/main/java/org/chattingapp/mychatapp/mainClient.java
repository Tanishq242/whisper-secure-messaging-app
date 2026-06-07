package org.chattingapp.mychatapp;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class mainClient {
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private mainCode app;
    public ProgressBar pbar;
    double temp = 0;
    int intTemp = 0;
    int num = 0;
    int counter = 0;
    Socket s = null;
    String tempFileName;
    readJsonFile readJsonFile = new readJsonFile();
    static msgEncodeDecode msg_encode = new msgEncodeDecode();
    static Map<String, String> fileDetail = new HashMap<>();

    //    String txt = null;
    // Method for sending message to server
    public static void senderMail(DataOutputStream dos, String text, Socket socket) {
        try {
//            System.out.println("Message is sent");
            dos.writeUTF(text);
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clientHandler(String mobile, mainCode app) {
        this.app = app;
        String id = mobile;
        // 🌀 Auto-Reconnect Loop
        while (s == null) {
            try {
                s = new Socket("localhost", 12345);
                System.out.println("✅ Connected to server.");
                app.serverStatus = "online"; // optional UI update
//                app.statusHandler();
                Platform.runLater(() -> {
                    if (app.serverStatusPopup.isShowing()) {
                        app.serverStatusPopup.hide();
                    }
                });
            } catch (IOException e) {
//                System.out.println("❌ Connection failed. Retrying in 5 seconds...");
                app.serverStatus = "offline";
                try {
                    Thread.sleep(5000); // Wait before retrying
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }

        try {
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            dos.writeUTF("CONNECTION:"+id);
            dos.flush();

            // Receiving Message and File from server
            new Thread(() -> {
                try {
                    DataInputStream dis = new DataInputStream(s.getInputStream());

                    while (true) {
                        String incoming = dis.readUTF();
                        System.out.println("CLIENT REC :" + incoming);
                        //File receiver block (Format: FILE_TRANSFER:FILE_NAME:FILE_LENGTH)
                        if (incoming.startsWith("FILE_TRANSFER:")) {
                            String[] parts = incoming.split(":");
                            String fileName = parts[1];
                            long fileSize = Long.parseLong(parts[2]);
                            int iteration = (int) (fileSize / 4096);
                            double unitValue = (double) 1 / iteration;

                            String filePath = "F:/Programs/My Projects/myChatApp/src/main/resources/encrypted files/" + fileName;

                            if (parts[parts.length - 1].equals("MRTB")) {
                                filePath = "F:/Programs/My Projects/myChatApp/splitRecFiles/" + fileName;
                            } else if (parts[parts.length - 1].equals("RP")) {
                                filePath = "F:/Programs/My Projects/myChatApp/tempFiles/" + fileName;
                            }

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
                                    System.out.println((++num) + " value: " + temp);
                                    Platform.runLater(() -> pbar.setProgress(temp));
                                }
                                if (temp < 1.0 || temp > 1.0) {
                                    Platform.runLater(() -> pbar.setProgress(1));
                                }
                            }

                            System.out.println("✅ File received: " + fileName);
                            if (parts[parts.length - 1].equals("MRTB")) {
                                Platform.runLater(() -> app.progressBarRemover(pbar));
                                System.out.println("File Split Downloaded");
                            } else if (parts[parts.length - 1].equals("RP")) {
                                String mobileNum = pbar.getId();
                                String correctName = readJsonFile.searchFileName(Long.parseLong(mobileNum), fileName);
                                int mergeStatus = msg_encode.mergeFile(fileName, correctName);
                                if (mergeStatus == 1) {
                                    Platform.runLater(() -> app.progressBarRemover(pbar, fileDetail, fileName));
                                }
                            } else {
                                Platform.runLater(() -> app.progressBarRemover(pbar, fileDetail, fileName));
                            }
                            // Acknowledge (optional)
                            // dos.writeUTF("Success: File received from server");
                        } else if (incoming.startsWith("FILE_TRANSFER_SPLIT:")) {
                            String[] parts = incoming.split(":");
                            String fileName = parts[1];
                            Label lb = app.fileDownloadUpdateLabel(fileName);
                            long fileSize = Long.parseLong(parts[2]);
                            String filePath = "F:/Programs/My Projects/myChatApp/splitRecFiles/" + fileName;
                            Boolean fileExistFlag = new File("F:/Programs/My Projects/myChatApp/splitRecFiles/" + fileName).exists();
                            String mobileNum = ((HBox) lb.getParent()).getChildren().getFirst().getId();
                            String correctName = readJsonFile.searchFileName(Long.parseLong(mobileNum), fileName);

                            if (fileExistFlag) {
                                filePath = "F:/Programs/My Projects/myChatApp/tempFiles/" + fileName;
                            }

                            if (parts[parts.length - 1].equals("CMPLT")) {
                                filePath = "F:/Programs/My Projects/myChatApp/src/main/resources/encrypted files/" + correctName;
                            }

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
                            }

                            if (fileExistFlag) {
                                Platform.runLater(() -> {
                                    ((Hyperlink) ((HBox) lb.getParent()).getChildren().getFirst()).setText("Processing...");
                                    lb.setText("merging and decrypting");
                                    lb.setPadding(new Insets(0, 0, 0, 50));
                                });
                                int mergeStatus = msg_encode.mergeFile(fileName, correctName);
                                String fileKey = readJsonFile.getFileKey(mobileNum, fileName);
                                byte[] decryptFileKeyBase64 = msg_encode.decryptFileKey(fileKey);
                                String base64EncodedKey = new String(decryptFileKeyBase64, StandardCharsets.UTF_8);
                                byte[] aesKeyBytes = Base64.getDecoder().decode(base64EncodedKey);
                                int decryptFileStatus = msg_encode.decryptFile(aesKeyBytes, fileName, correctName);
                                if (mergeStatus == 1 && decryptFileStatus == 1) {
                                    Platform.runLater(() -> app.completeDownloadUi(fileName));
                                } else {
                                    System.out.println("Some problem in merging or decrypting the file");
                                }
                            } else {
                                if (parts[parts.length - 1].equals("CMPLT")) {
                                    Platform.runLater(() -> {
                                        ((Hyperlink) ((HBox) lb.getParent()).getChildren().getFirst()).setText("Processing...");
                                        lb.setText("Decrypting");
                                        lb.setPadding(new Insets(0, 0, 0, 50));
                                    });
                                    String fileKey = readJsonFile.getFileKey(mobileNum, fileName);
                                    byte[] decryptFileKeyBase64 = msg_encode.decryptFileKey(fileKey);
                                    String base64EncodedKey = new String(decryptFileKeyBase64, StandardCharsets.UTF_8);
                                    byte[] aesKeyBytes = Base64.getDecoder().decode(base64EncodedKey);
                                    int decryptFileStatus = msg_encode.decryptFile(aesKeyBytes, correctName, correctName);
                                    if (decryptFileStatus == 1) {
                                        Platform.runLater(() -> app.downloadUpdate(fileName));
                                    } else {
                                        System.out.println("Some problem in decrypting the file");
                                    }
                                } else {
                                    Platform.runLater(() -> app.fileDownloadUiChange(fileName));
                                    app.w.updateSplitField(mobileNum, fileName, true);
                                }
                            }

                            System.out.println("✅ File received: " + fileName);
                        } else if (incoming.startsWith("SIGNAL:")) {
                            String[] parts = incoming.split(":");
                            Platform.runLater(() -> app.signalHandler(parts[1], parts[2]));
                        } else if (incoming.contains("true") || incoming.contains("false")) {
//                            System.out.println("USER STATUS: "+incoming);
                            String[] parts = incoming.split(":");
//                            Platform.runLater(() -> app.statusUpdate(parts[0], Boolean.parseBoolean(parts[1])));
                        } else if (incoming.startsWith("PENDING:")) {
//                            System.out.println("Received Message:" + incoming);
                            String[] parts = incoming.split(":");
                            String rawMsg = msg_encode.decrypt(parts[2], msg_encode.loadPrivateKey());
                            Platform.runLater(() -> app.msgRoute(parts[1] + ":" + rawMsg, parts[2], true));
                        } else if (incoming.startsWith("ACK:")) {
                            app.ackMsg = incoming;
                        } else {
                            if (incoming.startsWith("Success:")) {
//                                System.out.println(incoming);
                            } else if (incoming.contains(":sent a file:")) {
                                String[] str = incoming.split(":");
//                                System.out.println("File sent: "+str[2]);
                                Platform.runLater(() -> app.msgRoute(str[0] + ":" + "File sent:" + str[2] + ":" + str[3] + ":" + str[4] + ":" + str[str.length - 1], "Encrypted", false));
                            } else {
//                                System.out.println("Received Message:" + incoming);
                                String[] parts = incoming.split(":");
                                String rawMsg = msg_encode.decrypt(parts[1], msg_encode.loadPrivateKey());
                                Platform.runLater(() -> app.msgRoute(parts[0] + ":" + rawMsg, parts[1], true));
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("❌ Disconnected from server.");
                    e.printStackTrace();
                }
            }).start();

            // For sending message by getting input from client
            Scanner sc = new Scanner(System.in);
            String filePath;
            File file;
            FileInputStream fis;
            byte[] buffer = new byte[4096];
            int read;
            String[] arr = new String[3];
            // DataInputStream response_dis = new DataInputStream(s.getInputStream());
            while (true) {
                String txt = messageQueue.take();
//                System.out.println("received: "+txt);
                arr = txt.split(":");
//                System.out.println("length: "+arr.length);

                //File Sending Block (Format: ID:FILE_OFFER:file_location)
                if (txt.contains(":FILE_OFFER:")) {
                    filePath = arr[2] + ":" + arr[3];
                    System.out.println("FILE_OFFER: " + filePath);
                    file = new File(filePath);
                    fis = new FileInputStream(file);
                    dos.writeUTF("FILE_OFFER:" + arr[0] + ":" + file.getName() + ":" + arr[arr.length - 1]);
                    dos.writeLong(file.length());
                    while ((read = fis.read(buffer)) > 0) {
                        dos.write(buffer, 0, read);
                        // fis.close();
                    }
                    System.out.println("File sent: " + file.getName());
                    fis.close();
                    file.delete();
                } else if (arr.length == 2) { //Text message block (Format: ID:MSG)
                    if (txt.equals("none:disconnect")) {
                        try {
                            dos.close();
                            s.close();
                            sc.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break; // ✅ Exit the loop to avoid using closed socket
                    }

                    if (!txt.isEmpty()) {
                        senderMail(dos, "MSG:"+txt, s);
                    }

                } else if (txt.contains("FILE_REQ:")) { //File Request block from server (Format: FILE_REQ:file_name.ext)
                    String fileName = arr[1];
                    dos.writeUTF("FILE_REQ:" + fileName);
                    dos.flush();
                } else if (txt.startsWith("FILE_SPLIT:")) {
                    dos.writeUTF("FILE_REQ:" + arr[1] + ":" + arr[2]);
                    System.out.println("Sending file split request");
                } else if (txt.contains("SIGNAL:")) {
                    senderMail(dos, txt, s);
                } else if (txt.startsWith("PENDING:")) {
                    senderMail(dos, txt, s);
                }

            }

        } catch (Exception e) {
            app.serverStatus = "offline";
        }
    }

    public void sendWaitingMsg(String id, String message, String pkey) {
        try {
            String encoded = msg_encode.encrypt(message, msg_encode.loadPublicKeyFromBase64(pkey));
            messageQueue.offer("PENDING:" + id + ":" + encoded);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public String sendMessage(String id, String message, String pkey) {
        try {
            String encoded = msg_encode.encrypt(message, msg_encode.loadPublicKeyFromBase64(pkey));
//            System.out.println(encoded);
            messageQueue.offer(id + ":" + encoded);
            return encoded;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "Not Encrypted";
    }

    public void requestFile(String message, String fileName, String fileKey) {
        messageQueue.offer(message);
        fileDetail.put(fileName.trim(), fileKey);
    }

    public void requestFile(String message) {
        messageQueue.offer(message);
    }

    public void sendFile(File file, String message, String pKey) {
        try {
            String fileName = msg_encode.encryptFile(file);
            String fileKey = Base64.getEncoder().encodeToString(msgEncodeDecode.encodedFileKey);
            String encryptedFileKey = msg_encode.encrypt(fileKey, msg_encode.loadPublicKeyFromBase64(pKey));
            File encryptedFile = new File("src/main/resources/encrypted files/" + fileName);
            messageQueue.offer(message + ":" + encryptedFile.getAbsoluteFile().toString().replace("\\", "\\\\") + ":" + encryptedFileKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendSignal(String message) {
        messageQueue.offer(message);
    }
}