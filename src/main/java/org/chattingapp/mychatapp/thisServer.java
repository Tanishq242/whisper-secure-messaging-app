package org.chattingapp.mychatapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class thisServer {
    private static final String hostName = "localhost";
    private static final int portNumber = 12345;
    private static final int sleepTime = 5000;
    static boolean isConnected = false;
    private static mainCode app;
    public static Socket s;
    private static DataOutputStream dos;
    private static DataInputStream dis;
    private volatile Boolean isPresent = null;
    msgEncodeDecode encodeDecode = new msgEncodeDecode();

    thisServer(mainCode obj) {
        app = obj;
        createConnection();
    }

    public void createConnection() {
        try {
            s = new Socket(hostName, portNumber);
            dos = new DataOutputStream(s.getOutputStream());
            dis = new DataInputStream(s.getInputStream());

            isConnected = true;
            app.serverStatus = "online";
            app.statusHandler(s);

            String connectionMsg = "CONNECTION:" + app.mobile;
            dos.writeUTF(connectionMsg);
            dos.flush();
            // DON'T close dos here!

            // Start receiver in separate thread
            new Thread(() -> messageReceiver()).start();

            Platform.runLater(() -> {
                if (app.serverStatusPopup.isShowing()) {
                    app.serverStatusPopup.hide();
                }
            });
        } catch (IOException e) {
            System.err.println("Connection failed: " + e.getMessage());
            isConnected = false;
            app.serverStatus = "offline";
            try {
                Thread.sleep(sleepTime);
                createConnection(); // Retry
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void messageSender(String id, String msg, String pkey) {
        try {
            String encryptMsg = encodeDecode.encrypt(msg, encodeDecode.loadPublicKeyFromBase64(pkey));
            String msgFormat = "MSG" + ":" + id + ":" + encryptMsg;
            synchronized (dos) { // Thread-safe writing
                dos.writeUTF(msgFormat);
                dos.flush();
            }
            // DON'T close dos here!
        } catch (Exception e) {
            System.err.println("Failed to send message: " + e.getMessage());
            handleDisconnection();
        }
    }

    public static String stringBytes(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(b & 0xFF).append(",");
        }
        sb.setLength(sb.length() - 1); // remove last comma

        return sb.toString();
    }

    public void jsonDataSender(String groupId, String mobile) {
        try {
            JsonNode node = readJsonFile.searchGroup(groupId);
            assert node != null;
            System.out.println(node.toString());

            String aesKeyBase64 = Base64.getEncoder().encodeToString(msgEncodeDecode.encodedFileKey);
            String publicKey = backend_db.getPublicKey(Long.parseLong(mobile));
            String encryptedKey = encodeDecode.encrypt(aesKeyBase64,
                    encodeDecode.loadPublicKeyFromBase64(publicKey));
            byte[] encryptedKeyBytes = encryptedKey.getBytes(StandardCharsets.UTF_8);

            String stringBytes = stringBytes(node.toString());

            System.out.println("JSON DATA to BYTES: "+stringBytes);

            synchronized (dos) {
                // 1. Send command (UTF)
//                dos.writeUTF("JSON:" + mobile + ":" + stringBytes);

                // 2. Send target ID (long)
//                dos.writeLong(Long.parseLong(mobile));
//
//                // 3. Send encrypted JSON length + bytes
//                dos.writeInt(node.toString().length());
//                dos.write(new ObjectMapper().writeValueAsBytes(node));
//
//                // 4. Send encrypted AES key length + bytes
                dos.writeUTF("Sending Key:"+mobile);
                dos.writeInt(encryptedKeyBytes.length);
                dos.write(encryptedKeyBytes);

                dos.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void jsonDataReceiver() {
        try {
            // 1. ID
            long id = dis.readLong();

            // 2. JSON length
            int jsonLen = dis.readInt();

            // 3. JSON raw bytes
            byte[] encryptedJsonBytes = new byte[jsonLen];
            dis.readFully(encryptedJsonBytes);
            String encryptedJson = new String(encryptedJsonBytes, StandardCharsets.UTF_8);

            // 4. AES key length
            int keyLen = dis.readInt();

            // 5. AES key raw bytes
            byte[] encryptedKeyBytes = new byte[keyLen];
            dis.readFully(encryptedKeyBytes);
            String encryptedKey = new String(encryptedKeyBytes, StandardCharsets.UTF_8);

            // 6. decrypt AES key
            String aesKey = new String(
                    encodeDecode.decryptFileKey(encryptedKey),
                    StandardCharsets.UTF_8
            );

            // 7. decrypt JSON
            encodeDecode.decryptJsonData(aesKey, encryptedJson);

            Platform.runLater(() ->
                    mainCode.showAlert("Group Notification", "You are joined in the group")
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void messageReceiver() {
        try {
            while (isConnected && !s.isClosed()) {
                String msg = dis.readUTF();
                System.out.println(msg);
                String[] msgParts = msg.split(":");
                if (msg.contains(":sent a file:")) {
                    String msgFormat = msgParts[0] + ":File sent:" + msgParts[2] + ":" + msgParts[3] + ":" + msgParts[4] + ":" + msgParts[msgParts.length - 1];
                    Platform.runLater(() -> app.msgRoute(msgFormat, "Encrypted", false));
                } else if (msg.contains("true") || msg.contains("false")) {
                    Platform.runLater(() -> app.statusUpdate(msgParts[0], Boolean.parseBoolean(msgParts[1])));
                } else if (msg.contains(":DELETE_")) {
                    Platform.runLater(() -> app.signalHandler(msgParts[1], msgParts[2]));
                } else if (msg.startsWith("FILE_PRESENT:")) {
                    if (msg.equals("FILE_PRESENT:TRUE")) {
                        isPresent = true;
                    } else {
                        isPresent = false;
                        Platform.runLater(() -> mainCode.showAlert("File Not Found", "The requested file could not be found on the server."));
                    }
                } else if (msg.startsWith("GROUP_INVITE:")) {
                    Platform.runLater(() -> {
                        Boolean notificationFlag = app.notificationBox("Group", msgParts[3], msgParts[2]);
                        if (notificationFlag) groupJsonRequest(msgParts[1], msgParts[msgParts.length - 1]);
                    });
                } else if (msg.startsWith("GROUP_JSON_REQ:")) {
                    System.out.println("Yes, i received group json request");
                    jsonDataSender(msgParts[1], msgParts[msgParts.length - 1]);
                } else if (msg.startsWith("JSON_DATA_SENDING")) {
                    jsonDataReceiver();
                } else {
                    String decryptMsg = encodeDecode.decrypt(msgParts[1], encodeDecode.loadPrivateKey());
                    String rawMsg = msgParts[0] + ":" + decryptMsg;
                    Platform.runLater(() -> app.msgRoute(rawMsg, msgParts[1], true));
                }
            }
        } catch (Exception e) {
//            System.err.println("Connection lost: " + e.printStackTrace());
            e.printStackTrace();
            handleDisconnection();
        }
    }

    public void checkUserStatus(String id) {
        try {
            String msg = "SIGNAL:" + id + ":STATUS";
            synchronized (dos) { // Thread-safe writing
                dos.writeUTF(msg);
                dos.flush();
            }
            // DON'T close dos here!
        } catch (Exception e) {
            System.err.println("Failed to send message: " + e.getMessage());
            handleDisconnection();
        }
    }

    public void deleteMsgSignal(String msg) {
        try {
            synchronized (dos) { // Thread-safe writing
                dos.writeUTF(msg);
                dos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean fileExistCheck(String fileName, String extraCode) {
        try {
            String msg = "FILE_PRESENT:" + fileName + ":" + extraCode;
            dos.writeUTF(msg);
            dos.flush();

            // Wait for server response (isPresent should be updated from another thread)
            while (isPresent == null) {
                Thread.sleep(50); // small delay to avoid CPU 100%
            }

            return isPresent; // true / false
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void groupInviteSender(String groupId, String groupName, String groupAdmin, Map<Long, String> list) {
        try {
            String msg = null;
            for (long mobile : list.keySet()) {
                msg = "GROUP_INVITE:" + groupId + ":" + groupName + ":" + groupAdmin + ":" + mobile;
                synchronized (dos) {
                    dos.writeUTF(msg);
                    dos.flush();
                }
                Thread.sleep(50);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void groupJsonRequest(String groupId, String receiverId) {
        try {
            synchronized (dos) {
                String msg = "GROUP_JSON_REQ:" + groupId + ":" + receiverId;
                dos.writeUTF(msg);
                dos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDisconnection() {
        isConnected = false;
        app.serverStatus = "offline";
        try {
            if (dos != null) dos.close();
            if (dis != null) dis.close();
            if (s != null) s.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
        // Optionally retry connection
        createConnection();
    }
}