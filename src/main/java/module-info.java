module org.chattingapp.mychatapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.fasterxml.jackson.databind;
    requires google.genai;
    requires com.google.gson;
    requires okhttp3;
    requires org.json;
    requires org.bouncycastle.pkix;
    requires org.bouncycastle.provider;
    requires org.kordamp.ikonli.javafx;
    requires com.google.api.client;
    requires javafx.media;
    requires java.desktop;


    opens org.chattingapp.mychatapp to javafx.fxml;
    exports org.chattingapp.mychatapp;
}