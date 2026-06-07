package org.chattingapp.mychatapp;

public class exportChat {
    public String timestamp;
    public String sender;
    public String receiver;
    public String encrypted;
    public String type;

    // Optional fields for files
    public String fileName;
    public Boolean fileEncrypted;

    public exportChat(String timestamp, String sender, String receiver, String encrypted, String type) {
        this.timestamp = timestamp;
        this.sender = sender;
        this.receiver = receiver;
        this.encrypted = encrypted;
        this.type = type;
    }

    // Overloaded constructor for file messages
    public exportChat(String timestamp, String sender, String receiver, String encrypted, String type, String fileName, boolean fileEncrypted) {
        this(timestamp, sender, receiver, encrypted, type);
        this.fileName = fileName;
        this.fileEncrypted = fileEncrypted;
    }
}
