package org.chattingapp.mychatapp;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.util.Base64;

public class fileUpload {
    static msgEncodeDecode msg_encode = new msgEncodeDecode();
    public static void send(long clientId, String id, File f, String pkey) {
        try {
            String fileName = msg_encode.encryptFile(f);
            String fileKey = Base64.getEncoder().encodeToString(msgEncodeDecode.encodedFileKey);
            String encryptedFileKey = msg_encode.encrypt(fileKey, msg_encode.loadPublicKeyFromBase64(pkey));

            Socket s = new Socket("localhost", 12347);
            System.out.println("✅ Connected to File Server.");
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            File file = new File("src/main/resources/encrypted files/"+fileName);
            FileInputStream fis = new FileInputStream(file);
            String msg = id + ":" + file.getName().trim() + ":" + encryptedFileKey;
            System.out.println("FILE_OFFER: "+msg);
            dos.writeLong(clientId); // SENDER ID
            dos.writeUTF(msg); // ID:FILE_NAME:FILE_KEY
            dos.writeLong(file.length()); // FILE_SIZE

            byte[] buffer = new byte[4096];
            int read;
            while ((read = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, read);
            }
            System.out.println("File sent: " + file.getName());
            fis.close();
            dos.close();
            s.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
