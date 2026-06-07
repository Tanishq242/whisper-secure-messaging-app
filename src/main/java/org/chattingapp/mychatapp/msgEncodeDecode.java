package org.chattingapp.mychatapp;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class msgEncodeDecode {
    // Load the JKS keystore
    String jksPath = "src/main/resources/JKS/keystore.jks";
    String keystorePassword;
    String alias = "mykey";
    KeyStore keyStore;
    FileInputStream fis;
    private static PrivateKey privateKey;
    private static SecretKey SECRET_KEY;
    private static SecretKey FILE_KEY;
    static byte[] encodedFileKey;

    public void setUpCode(String pass) {
        try {
            this.keystorePassword = pass;
            keyStore = KeyStore.getInstance("JKS");
            fis = new FileInputStream(jksPath);
            keyStore.load(fis, keystorePassword.toCharArray());
            this.privateKey = (PrivateKey) keyStore.getKey(alias, keystorePassword.toCharArray());
            System.out.println("JKS unlocked successfully");
            getAESKey(pass);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void getAESKey(String password) {
        try {
            // 1. Load the KeyStore
            KeyStore keyStore = KeyStore.getInstance("JCEKS");
            try (FileInputStream fis = new FileInputStream("src/main/resources/AES key/aesKey.jks")) {
                keyStore.load(fis, password.toCharArray());
            }

            // 2. Prepare password protection
            KeyStore.PasswordProtection keyPassword = new KeyStore.PasswordProtection(password.toCharArray());

            // 3. Retrieve AES-128 key
            KeyStore.Entry entry128 = keyStore.getEntry("aes128key", keyPassword);
            if (entry128 instanceof KeyStore.SecretKeyEntry) {
                SECRET_KEY = ((KeyStore.SecretKeyEntry) entry128).getSecretKey();
                System.out.println("AES-128 key loaded into SECRET_KEY.");
            } else {
                System.out.println("AES-128 key not found.");
            }

            // 4. Retrieve AES-256 key
            KeyStore.Entry entry256 = keyStore.getEntry("aes256key", keyPassword);
            if (entry256 instanceof KeyStore.SecretKeyEntry) {
                FILE_KEY = ((KeyStore.SecretKeyEntry) entry256).getSecretKey();
                encodedFileKey = FILE_KEY.getEncoded();
                System.out.println("AES-256 key loaded into FILE_KEY.");
            } else {
                System.out.println("AES-256 key not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Get the private key from the jks file
    public PrivateKey loadPrivateKey() {
        return privateKey;
    }

    //Get the public key from the jks file
    public PublicKey loadPublicKey() throws Exception {
        Certificate certificate = keyStore.getCertificate(alias);
        return certificate.getPublicKey();
    }

    //Convert public key -> Base64
    public String convertPublicKeyToBase64(PublicKey publicKey) {
        byte[] encodedKey = publicKey.getEncoded(); // X.509 encoded format
        return Base64.getEncoder().encodeToString(encodedKey);
    }

    //Convert Base64 -> public key
    public PublicKey loadPublicKeyFromBase64(String base64Key) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    public static String groupPrivateKey(String keystorePassword) {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream("src/main/resources/JKS/groupKey.jks");
            keyStore.load(fis, keystorePassword.toCharArray());
            fis.close();

            // Extract private key
            Key privateKeyObj = keyStore.getKey("groupPrivateKey", keystorePassword.toCharArray());
            String privateKeyBase64 = null;
            if (privateKeyObj instanceof PrivateKey) {
                privateKeyBase64 = Base64.getEncoder().encodeToString(privateKeyObj.getEncoded());
            }
            return privateKeyBase64;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String groupPublicKey(String keystorePassword) {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream("src/main/resources/JKS/groupKey.jks");
            keyStore.load(fis, keystorePassword.toCharArray());
            fis.close();

            // Extract public key from certificate
            Certificate cert = keyStore.getCertificate("groupPublicKey");
            String publicKeyBase64 = null;
            if (cert != null) {
                PublicKey publicKey = cert.getPublicKey();
                publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            }

            return publicKeyBase64;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Encrypt with public key
    public String encrypt(String plainText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // Common RSA transformation
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes); // return as Base64 string
    }

    // Decrypt with private key
    public String decrypt(String encryptedText, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes);
    }

    public byte[] decryptFileKey(String encryptedText) throws Exception {
        if (privateKey == null) {
            System.out.println("Private Key is null!");
        }
        System.out.println(encryptedText);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(Base64.getDecoder().decode(encryptedText));
    }

    public String symmetricEncrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // Generate a random 16-byte IV
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Initialize cipher for encryption with key and IV
        cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY, ivSpec);

        // Encrypt the data
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());

        // Combine IV + encrypted bytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(iv);
        outputStream.write(encryptedBytes);
        byte[] ivAndCipherText = outputStream.toByteArray();

        // Encode to Base64 for storage/transmission
        return Base64.getEncoder().encodeToString(ivAndCipherText);
    }

    public String symmetricDecrypt(String encryptedText) throws Exception {
        // Decode from Base64
        byte[] ivAndCipherText = Base64.getDecoder().decode(encryptedText);

        // Extract IV and ciphertext
        byte[] iv = Arrays.copyOfRange(ivAndCipherText, 0, 16);
        byte[] cipherText = Arrays.copyOfRange(ivAndCipherText, 16, ivAndCipherText.length);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Initialize cipher for decryption with same key and IV
        cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY, ivSpec);

        // Decrypt
        byte[] decryptedBytes = cipher.doFinal(cipherText);
        return new String(decryptedBytes);
    }

    public String encryptFile(File inputFile) {
        try {
            // Generate a random IV
            byte[] iv = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, FILE_KEY, ivSpec);

            // Read file
            FileInputStream fis = new FileInputStream(inputFile);
            byte[] inputBytes = fis.readAllBytes();
            fis.close();

            // Encrypt
            byte[] encryptedBytes = cipher.doFinal(inputBytes);

            // Write IV + encrypted data to output
            FileOutputStream fos = new FileOutputStream("src/main/resources/encrypted files/encrypted_" + inputFile.getName());
            fos.write(iv); // Write IV first (16 bytes)
            fos.write(encryptedBytes);
            fos.close();
            return "encrypted_" + inputFile.getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int decryptFile(byte[] aesKeyBytes, String fileName, String correctFileName) {
        try {
            System.out.println("File name is " + fileName);
            System.out.println("Correct File name is " + correctFileName);

//            Fix: if error with split transfer change file name code
            FileInputStream fis = new FileInputStream("F:/Programs/My Projects/myChatApp/src/main/resources/encrypted files/" + correctFileName);

            // Read IV (first 16 bytes)
            byte[] iv = new byte[16];
            fis.read(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Read rest of the file (encrypted data)
            byte[] encryptedData = fis.readAllBytes();
            fis.close();

            // Decrypt
            SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);
            byte[] decryptedBytes = cipher.doFinal(encryptedData);

            // Write output
            FileOutputStream fos = new FileOutputStream("Received/" + correctFileName.replace("encrypted_", ""));
            fos.write(decryptedBytes);
            fos.close();

            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int decryptJsonData(String aesKey, String encryptedData) throws Exception {
        // 1. Decode Base64 encrypted data
        byte[] ivAndCipherText = Base64.getDecoder().decode(encryptedData);

        // 2. Extract IV (first 16 bytes)
        byte[] iv = Arrays.copyOfRange(ivAndCipherText, 0, 16);

        // 3. Extract ciphertext (remaining bytes)
        byte[] cipherBytes = Arrays.copyOfRange(ivAndCipherText, 16, ivAndCipherText.length);

        // 4. Decode AES key (Base64 → bytes)
        byte[] decodedKey = Base64.getDecoder().decode(aesKey);
        SecretKeySpec secretKey = new SecretKeySpec(decodedKey, "AES");

        // 5. Initialize cipher for decryption
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        // 6. Decrypt
        byte[] decryptedBytes = cipher.doFinal(cipherBytes);
        String decryptedText = new String(decryptedBytes, StandardCharsets.UTF_8);

        // 7. Print and return
        System.out.println("Decrypted Text: " + decryptedText);
        return 1;
    }

    public int mergeFile(String fileName, String correctFileName) {
        try {
            // Read file into byte array
            byte[] part1 = Files.readAllBytes(Paths.get("F:/Programs/My Projects/myChatApp/splitRecFiles/" + fileName));
            byte[] part2 = Files.readAllBytes(Paths.get("F:/Programs/My Projects/myChatApp/tempFiles/" + fileName));

            byte[] mergeFile = new byte[part1.length + part2.length];

            // Copy first part
            System.arraycopy(part1, 0, mergeFile, 0, part1.length);

            // Copy second part
            System.arraycopy(part2, 0, mergeFile, part1.length, part2.length);

            FileOutputStream fos = new FileOutputStream("F:/Programs/My Projects/myChatApp/src/main/resources/encrypted files/" + correctFileName);
            fos.write(mergeFile);
            fos.close();
            System.out.println("Files merged successfully!");
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void main(String[] args) throws Exception {
        msgEncodeDecode obj = new msgEncodeDecode();
        obj.setUpCode("12345678");
        String code = obj.symmetricEncrypt("Tanishq");
        byte[] bytes = code.getBytes(StandardCharsets.UTF_8);
        System.out.println(Arrays.toString(bytes));
        System.out.println(bytes.length);
    }
}