package org.chattingapp.mychatapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class test {
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static void stringToJsonFormatter(String jsonString) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonString);

        String formatted = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(jsonNode);

        System.out.println(formatted);
    }

    public static String jsonDataSender(String groupId) {
        try {
            JsonNode node = readJsonFile.searchGroup(groupId);
            assert node != null;
            System.out.println(node.toString());
            return node.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String symmetricDecrypt(String base64Key, String encryptedBase64) throws Exception {

        // 1. Decode the Base64 AES key (this produces a 32-byte AES-256 key)
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        System.out.println("Key length = " + keyBytes.length);
        System.out.println("AES KEY HEX = " + bytesToHex(keyBytes));

        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        // 2. Decode the encrypted Base64 string (IV + ciphertext)
        byte[] ivAndCipherText = Base64.getDecoder().decode(encryptedBase64);

        // 3. Extract IV (first 16 bytes)
        byte[] iv = Arrays.copyOfRange(ivAndCipherText, 0, 16);

        // 4. Extract ciphertext (remaining bytes)
        byte[] cipherText = Arrays.copyOfRange(ivAndCipherText, 16, ivAndCipherText.length);

        // 5. Decrypt
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

        byte[] decryptedBytes = cipher.doFinal(cipherText);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        try {
//            String words = jsonDataSender("5312658740");
            String words = "Hello world\nHow are you?";
            msgEncodeDecode obj = new msgEncodeDecode();
            backend_db db = new backend_db();
            obj.setUpCode("12345678");
            obj.getAESKey("12345678");
            String aesKey = Base64.getEncoder().encodeToString(msgEncodeDecode.encodedFileKey);
            String encryptedJson = obj.symmetricEncrypt(words);
            String public_key = backend_db.getPublicKey(8595079238L);
            String encryptedKey = obj.encrypt(aesKey, obj.loadPublicKey());
//            System.out.println("AES KEY HEX ENCRYPTION = " + bytesToHex(Base64.getDecoder().decode(encryptedKey)));

            System.out.println("ENCRYPTED JSON: " + encryptedJson);
            System.out.println("AES KEY: " + aesKey);
            System.out.println("ENCRYPTED KEY: " + encryptedKey);

            byte[] decryptFileKeyBase64 = obj.decryptFileKey(encryptedKey);
            System.out.println("AFTER DECRYPTION AES KEY: " + new String(decryptFileKeyBase64, StandardCharsets.UTF_8));
            symmetricDecrypt(new String(decryptFileKeyBase64, StandardCharsets.UTF_8), encryptedJson);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}