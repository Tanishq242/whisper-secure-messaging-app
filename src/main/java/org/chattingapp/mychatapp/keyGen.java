package org.chattingapp.mychatapp;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

public class keyGen {
    public int generateKey(String password) {
        try {
            Security.addProvider(new BouncyCastleProvider());

            // 1. Generate RSA KeyPair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();

            // 2. Create self-signed certificate using modern BouncyCastle
            X509Certificate certificate = generateSelfSignedCertificate(keyPair);

            // 3. Save to JKS
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, null);  // initialize empty keystore

            keyStore.setKeyEntry("mykey", keyPair.getPrivate(), password.toCharArray(), new java.security.cert.Certificate[]{certificate});

            FileOutputStream fos = new FileOutputStream("src/main/resources/JKS/keystore.jks");
            keyStore.store(fos, password.toCharArray());

            System.out.println("Keystore 'keystore.jks' generated successfully.");
            return 1;

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair) throws Exception {
        long now = System.currentTimeMillis();
        Date startDate = new Date(now);
        Date endDate = new Date(now + (100L * 365 * 24 * 60 * 60 * 1000)); // valid for 100 year

        X500Principal subject = new X500Principal("CN=Test, O=MyOrg, C=IN");

        BigInteger serial = BigInteger.valueOf(now);

        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                subject, serial, startDate, endDate, subject, keyPair.getPublic());

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider("BC")
                .build(keyPair.getPrivate());

        X509CertificateHolder certHolder = certBuilder.build(signer);

        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certHolder);
    }

    public static void changeJksPass(String oldPass, String newPass) {
        String keystorePath = "src/main/resources/JKS/keystore.jks";
        char[] oldPassword = oldPass.toCharArray();
        char[] newPassword = newPass.toCharArray();

        try {
            // 1. Load existing keystore
            FileInputStream fis = new FileInputStream(keystorePath);
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(fis, oldPassword);
            fis.close();

            // 2. Save keystore using new password
            FileOutputStream fos = new FileOutputStream(keystorePath);
            keystore.store(fos, newPassword);
            fos.close();

            System.out.println("Keystore password changed successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int AES_Key_Generator(String password) {
        try {
            // 1. Generate AES-128 Key
            KeyGenerator keyGen128 = KeyGenerator.getInstance("AES");
            keyGen128.init(128);
            SecretKey secretKey128 = keyGen128.generateKey();

            // 2. Generate AES-256 Key
            KeyGenerator keyGen256 = KeyGenerator.getInstance("AES");
            keyGen256.init(256);
            SecretKey secretKey256 = keyGen256.generateKey();

            // 3. Create or load KeyStore
            KeyStore keyStore = KeyStore.getInstance("JCEKS");
            keyStore.load(null, null); // create new empty keystore

            // 4. Prepare password protection
            KeyStore.PasswordProtection keyPassword = new KeyStore.PasswordProtection(password.toCharArray());

            // 5. Store AES-128 key
            KeyStore.SecretKeyEntry entry128 = new KeyStore.SecretKeyEntry(secretKey128);
            keyStore.setEntry("aes128key", entry128, keyPassword);

            // 6. Store AES-256 key
            KeyStore.SecretKeyEntry entry256 = new KeyStore.SecretKeyEntry(secretKey256);
            keyStore.setEntry("aes256key", entry256, keyPassword);

            // 7. Save KeyStore to file
            try (FileOutputStream fos = new FileOutputStream("src/main/resources/AES key/aesKey.jks")) {
                keyStore.store(fos, password.toCharArray());
            }

            System.out.println("KeyStore created with AES-128 and AES-256 keys stored.");

            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int createGroupKey(String password) {
        try {
            String jksFilePath = "F:\\Programs\\My Projects\\myChatApp\\src\\main\\resources\\JKS\\groupKey.jks";

            Security.addProvider(new BouncyCastleProvider());

            // 1. Generate RSA KeyPair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();

            // 2. Create self-signed certificate
            X509Certificate certificate = generateSelfSignedCertificate(keyPair);

            // 3. Save to JKS
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, null);

            // Store private key with certificate chain
            keyStore.setKeyEntry("groupPrivateKey", keyPair.getPrivate(),
                    password.toCharArray(), new Certificate[]{certificate});

            // Store certificate (contains public key)
            keyStore.setCertificateEntry("groupPublicKey", certificate);

            FileOutputStream fos = new FileOutputStream(jksFilePath);
            keyStore.store(fos, password.toCharArray());
            fos.close();

            System.out.println("Keystore created successfully at: " + jksFilePath);
            return 1;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}