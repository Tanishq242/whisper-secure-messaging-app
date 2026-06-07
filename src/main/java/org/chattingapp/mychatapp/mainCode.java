package org.chattingapp.mychatapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.UnaryOperator;

public class mainCode extends Application {
    Stage commanStage;
    int appWidth = 1075, appHeight = 600;
    int iNum = 0;
    private static String userPassword = "12345678";
    String tempStrStore = null;
    String tempStr;
    String responseMsg = null;
    String senderEncryption;
    String openMsgBoxId;
    String ackMsg = null;
    int res;
    final int OTP_LENGTH = 4;
    int count = 60;
    boolean flag = false;
    int ind = -1;
    int msgCounter = -1;
    int clkCount = 0;
    VBox refBox;
    HBox tempRef;
    StackPane tempRef2;
    VBox musicTempBox;
    Scene loginScene, signupScene;
    Long mobile;
    boolean isDark = false;
    ArrayList<VBox> vboxList = new ArrayList<>();
    ListView<String> langList = new ListView<>();
    Map<String, String> languages = new LinkedHashMap<>();
    LinkedHashMap<String, String> list = new LinkedHashMap<>();
    LinkedHashMap<String, VBox> filesUIRef = new LinkedHashMap<>();
    ArrayList<String> onlineIdList = new ArrayList<>();
    ArrayList<String> fileNameList = new ArrayList<>();
    private double offsetX = 0;
    private double offsetY = 0;
    private double sOffsetX = 0;
    private double sOffsetY = 0;
    int xserverPopup = 1100;
    int yserverPopup = 450;
    String currentUserName;
    VBox ca, barRemover, notifyBox;
    StackPane tempBox;
    Popup popupForReply;
    Popup popupForEmoji;
    Popup popupForAttachment;
    Popup serverStatusPopup = new Popup();
    String serverStatus = "online";
    Boolean isFile = false;
    File selected_File;
    Boolean botFlag = false;
    LocalDateTime currentTime;
    ArrayNode node;
    boolean dataInsertResponse = false;
    boolean loginFlag = false;
    boolean receiveFolderClickFlag = false;
    boolean isMediaBox = false;

    thisServer clientServer;
    musicPlayerUI musicPlayer = new musicPlayerUI();
    imageViewerUI imageViewer = new imageViewerUI();
    backend_db db = new backend_db();
    keyGen keygen = new keyGen();
    mainClient client = new mainClient();
    msgEncodeDecode msgEncodeDecode = new msgEncodeDecode();
    readJsonFile rdf = new readJsonFile();
    fileReader readFile = new fileReader();
    botCode botcode = new botCode();
    writeJsonFile w = new writeJsonFile();
    translatorCode translateRequest = new translatorCode();
    ExecutorService executors = Executors.newSingleThreadExecutor();
    private final Object ackLock = new Object();

    public static void main(String[] args) {

        launch(args);
    }

    public String getCurrentTime() {
        currentTime = LocalDateTime.parse(LocalDateTime.now().toString());

        // Format to keep only 2 digits of milliseconds (rounded)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS");

        String formatted = currentTime.format(formatter);

        return formatted;
    }

    public String dateModifier(String fileDate) {
        String[] parts = fileDate.split(" ");
        String[] nums = parts[0].split("-");
        String correctDate = nums[2] + "-" + nums[1] + "-" + nums[0];
        return correctDate;
    }

    public String calculateRemainingDays(String fileDate) {
        String[] parts = fileDate.split(" ");
        String[] nums = parts[0].split("-");
        System.out.print(nums[2]);
        System.out.print(nums[1]);
        System.out.println(nums[0]);
        LocalDate previousDate = LocalDate.of(Integer.parseInt(nums[0]), Integer.parseInt(nums[1]), Integer.parseInt(nums[2]));
        long totalDays = ChronoUnit.DAYS.between(previousDate, LocalDate.now());
        return String.valueOf(7 - totalDays);
    }

    private void changeScene(Scene scene, Event e) {
        Node node = (Node) e.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.setScene(scene);
    }

    public Label fileDownloadUpdateLabel(String fileId) {
        VBox vb1 = filesUIRef.get(fileId);
        vb1.setOnMouseClicked(null);
        return (Label) ((HBox) vb1.getChildren().getLast()).getChildren().getLast();
    }

    public void fileDownloadUiChange(String fileId) {
        VBox vb1 = filesUIRef.get(fileId);
        ImageView iv1 = new ImageView(new Image("fileHalf.png"));
        iv1.setId("halfFile");
        iv1.setFitWidth(15);
        iv1.setFitHeight(15);
        ((Label) vb1.getChildren().get(1)).setGraphic(iv1);
        ((Label) ((HBox) vb1.getChildren().getLast()).getChildren().getLast()).setText("");
        ((Label) ((HBox) vb1.getChildren().getLast()).getChildren().getLast()).setPadding(new Insets(0, 0, 0, 120));
        Hyperlink link = ((Hyperlink) ((HBox) vb1.getChildren().getLast()).getChildren().getFirst());
        link.setText("Download Remaining");
        link.setDisable(false);
        link.setOnAction(e -> {
//            client.requestFile("FILE_SPLIT:" + fileId + ":SPLIT");
            Label lb = fileDownloadUpdateLabel(fileId);
            fileDownload.alternateReceiver(fileId, "RP", lb);
            link.setText("Downloading...");
            link.setOnAction(null);
            link.setDisable(true);
        });
    }

    public void completeDownloadUi(String fileId) {
        VBox vb1 = filesUIRef.get(fileId);
        ImageView iv1 = new ImageView(new Image("tick.png"));
        iv1.setFitWidth(15);
        iv1.setFitHeight(15);
        ((Label) vb1.getChildren().get(1)).setGraphic(iv1);
        ((Label) ((HBox) vb1.getChildren().getLast()).getChildren().getLast()).setText("");
        ((Label) ((HBox) vb1.getChildren().getLast()).getChildren().getLast()).setPadding(new Insets(0, 0, 0, 180));
        Hyperlink link = ((Hyperlink) ((HBox) vb1.getChildren().getLast()).getChildren().getFirst());
        link.setText("Open File");
        link.setDisable(false);

        link.setOnAction(e -> {
            playMusic(((Label) vb1.getChildren().get(1)).getText().replace("encrypted_", ""));
        });

        w.updateSplitField(link.getId(), fileId, false);
        w.updateDownloadField(link.getId(), fileId);

        readFile.deleteFile("F:/Programs/My Projects/myChatApp/tempFiles/" + fileId);
        readFile.deleteFile("F:/Programs/My Projects/myChatApp/splitRecFiles" + fileId);
        readFile.deleteFile("F:/Programs/My Projects/myChatApp/src/main/resources/encrypted files/" + rdf.searchFileName(Long.parseLong(link.getId()), fileId));
    }

    public void downloadUpdate(String fileId) {
        VBox vb1 = filesUIRef.get(fileId);
        ImageView iv1 = new ImageView(new Image("tick.png"));
        iv1.setFitWidth(15);
        iv1.setFitHeight(15);
        ((Label) vb1.getChildren().get(1)).setGraphic(iv1);
        ((Label) ((HBox) vb1.getChildren().getLast()).getChildren().getLast()).setText("");
        ((Label) ((HBox) vb1.getChildren().getLast()).getChildren().getLast()).setPadding(new Insets(0, 0, 0, 180));
        Hyperlink link = ((Hyperlink) ((HBox) vb1.getChildren().getLast()).getChildren().getFirst());
        link.setText("Open File");
        link.setDisable(false);

        link.setOnAction(e -> {
            playMusic(((Label) vb1.getChildren().get(1)).getText());
        });


        w.updateDownloadField(link.getId(), fileId);
    }

    public static void openFile(File file) {
        if (!file.exists()) {
            showAlert("File Not Found", "❌ File not found:\n" + file.getAbsolutePath());
            return;
        }

        if (!Desktop.isDesktopSupported()) {
            showAlert("Not Supported", "❌ Desktop operations are not supported on this system.");
            return;
        }

        Desktop desktop = Desktop.getDesktop();

        try {
            desktop.open(file);
            System.out.println("✅ File opened successfully: " + file.getAbsolutePath());
        } catch (UnsupportedOperationException e) {
            showAlert("Operation Unsupported", "⚠️ This system does not support opening files automatically.");
        } catch (IOException e) {
            showAlert("No Application Found", "⚠️ No supported application found to open this file:\n" + file.getName());
        } catch (Exception e) {
            showAlert("Error", "⚠️ Unexpected error:\n" + e.getMessage());
        }
    }

    public static void showAlert(String title, String message) {
        // Create and show a JavaFX Alert
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void playMusic(String songName) {
        HBox root = (HBox) commanStage.getScene().getRoot();
        String nodeId = root.getChildren().getLast().getId();
        if (nodeId == null) {
            VBox rightBox = musicPlayer.musicPlayer(songName, isDark);
            rightBox.setAlignment(Pos.CENTER);
            root.getChildren().add(rightBox);
            musicTempBox = rightBox;
            root.setHgrow(rightBox, Priority.ALWAYS);
            musicPlayer.loadMedia(new File("./Received/" + songName));
            isMediaBox = true;
            if (isDark) rightBox.setStyle("-fx-background-color: #283747");
            new Thread(() -> musicPlayer.togglePlayPause()).start();
        } else {
            root.getChildren().removeLast();
            VBox rightBox = musicPlayer.musicPlayer(songName, isDark);
            rightBox.setAlignment(Pos.CENTER);
            root.getChildren().add(rightBox);
            root.setHgrow(rightBox, Priority.ALWAYS);
            musicPlayer.loadMedia(new File("./Received/" + songName));
            isMediaBox = true;
            musicPlayer.isPlaying = false;
            if (isDark) rightBox.setStyle("-fx-background-color: #283747");
            new Thread(() -> musicPlayer.togglePlayPause()).start();
        }
    }

    public void imageViewer(String imageName) {
        HBox root = (HBox) commanStage.getScene().getRoot();
        String nodeId = root.getChildren().getLast().getId();
        if (nodeId == null) {
            BorderPane rightBox = imageViewer.imageViewer();
            imageViewer.openImage(new File("./Received/" + imageName));
            root.getChildren().add(rightBox);
            root.setHgrow(rightBox, Priority.ALWAYS);
            isMediaBox = true;
        }
    }

    private void animateDot(Circle dot, int delayMillis) {
        TranslateTransition bounce = new TranslateTransition(Duration.millis(800), dot);
        bounce.setByY(-10);
        bounce.setAutoReverse(true);
        bounce.setCycleCount(Animation.INDEFINITE);
        bounce.setDelay(Duration.millis(delayMillis));
        bounce.play();
    }

    public HBox bufferingUI() {
        ProgressIndicator pi = new ProgressIndicator();
        pi.setMaxSize(15, 15);
        pi.setStyle("-fx-progress-color: red;");
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER);
        Label label = new Label("Decrypting File");
        box.getChildren().addAll(pi, label);
        return box;
    }

    public void progressBarRemover(ProgressBar pb, Map<String, String> list, String fileName) {
        try {
            VBox vb = (VBox) pb.getParent();
            ImageView iv1 = new ImageView(new Image("tick.png"));
            iv1.setFitWidth(15);
            iv1.setFitHeight(15);
            barRemover.getChildren().remove(pb);
//            System.out.println("1: "+fileName);
            barRemover.getChildren().add(bufferingUI());
//            System.out.println("2: "+fileName);
//            System.out.println("3: "+list.get(fileName));
//            System.out.println("4: "+list);
            byte[] decryptFileKeyBase64 = msgEncodeDecode.decryptFileKey(list.get(fileName));
            String base64EncodedKey = new String(decryptFileKeyBase64, StandardCharsets.UTF_8);
            byte[] aesKeyBytes = Base64.getDecoder().decode(base64EncodedKey);
            String correctFileName = rdf.searchFileName(Long.parseLong(pb.getId()), fileName);
            int decryptFileStatus = msgEncodeDecode.decryptFile(aesKeyBytes, fileName, correctFileName);
            if (decryptFileStatus == 1) {
                System.out.println("file decrypted successfully");
                File file = new File("src/main/resources/encrypted files/" + fileName);
                barRemover.getChildren().removeLast();
                ((Label) vb.getChildren().getFirst()).setGraphic(iv1);
                file.delete();
            } else {
                System.out.println("Problem occurred in decrypting file");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void progressBarRemover(ProgressBar pb, VBox tempbar, String fileName, String fileKey) {
        try {
            ImageView iv1 = new ImageView(new Image("tick.png"));
            iv1.setFitWidth(15);
            iv1.setFitHeight(15);

//            System.out.println("List: "+tempBox.getChildren());
            tempbar.getChildren().remove(pb);
            tempbar.getChildren().add(bufferingUI());
//            System.out.println("1: "+fileName);

//            System.out.println("2: "+fileName);
//            System.out.println("3: "+list.get(fileName));
//            System.out.println("4: "+list);
            byte[] decryptFileKeyBase64 = msgEncodeDecode.decryptFileKey(fileKey);
            String base64EncodedKey = new String(decryptFileKeyBase64, StandardCharsets.UTF_8);
            byte[] aesKeyBytes = Base64.getDecoder().decode(base64EncodedKey);
            System.out.println(pb.getId());
            System.out.println(fileName);
//            String correctFileName = rdf.searchFileName(Long.parseLong(pb.getId()), fileName);
            int decryptFileStatus = msgEncodeDecode.decryptFile(aesKeyBytes, fileName, fileName);
            if (decryptFileStatus == 1) {
                System.out.println("file decrypted successfully");
                File file = new File("src/main/resources/encrypted files/" + fileName);
                ((Label) tempbar.getChildren().getFirst()).setGraphic(iv1);
                tempbar.getChildren().removeLast();
                file.delete();
                w.updateSplitField(pb.getId(), rdf.getFileId(pb.getId(), fileName), false);
                w.updateDownloadField(pb.getId(), rdf.getFileId(pb.getId(), fileName));
            } else {
                System.out.println("Problem occurred in decrypting file");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void progressBarRemover(ProgressBar pb) {
        VBox vb = (VBox) pb.getParent();
        System.out.println(vb.getChildren());
        ImageView iv1 = new ImageView(new Image("fileHalf.png"));
        iv1.setId("split-file");
        iv1.setFitWidth(15);
        iv1.setFitHeight(15);
        ((Label) vb.getChildren().getFirst()).setGraphic(iv1);
        pb.setProgress(0);
        pb.setVisible(false);
        pb.setManaged(false);
    }

    public Boolean notificationBox(String notificationType, String personName, String groupName) {
        if (notificationType.equals("Group")) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Group Invitation");
            alert.setHeaderText("You have a new group invitation");
            alert.setContentText(personName + " invited you to join \"" + groupName + "\".");

            // Replace default OK/CANCEL with Accept/Decline
            ButtonType accept = new ButtonType("Accept", ButtonBar.ButtonData.OK_DONE);
            ButtonType decline = new ButtonType("Decline", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(accept, decline);

            Optional<ButtonType> result = alert.showAndWait();

            // Returns true if user clicks Accept
            return result.isPresent() && result.get() == accept;
        }
        return false;
    }

    public void originalMode(VBox midLowerBox, VBox settingMid, VBox importMid, VBox filesBox) {
        if (!serverStatusPopup.getContent().isEmpty()) {
            Label serverStatus = (Label) serverStatusPopup.getContent().getFirst();
            serverStatus.setStyle("-fx-text-fill: black;");
        }

        for (int i = 0; i < vboxList.size(); i++) {
            vboxList.get(i).setStyle("-fx-background-color: #f6f5f8");
            StackPane stackPane = (StackPane) vboxList.get(i).getChildren().getFirst();
            HBox hb1 = (HBox) stackPane.getChildren().get(1);
            hb1.getChildren().getFirst().setStyle("-fx-text-fill: black;");
            HBox hb2 = (HBox) hb1.getChildren().getLast();
            hb2.getChildren().getLast().setStyle("-fx-text-fill: black;");
            stackPane.getChildren().getLast().setStyle("-fx-text-fill: black;");
            ScrollPane sp = (ScrollPane) vboxList.get(i).getChildren().get(1);
            sp.getContent().setStyle("-fx-background-color: #f6f5f8");

            VBox x1 = (VBox) midLowerBox.getChildren().get(i);
            HBox x2 = (HBox) x1.getChildren().getFirst();
            HBox x3 = (HBox) x2.getChildren().get(1);
            x3.getChildren().getFirst().setStyle("-fx-text-fill: black;");

            x1.getStyleClass().remove("chatListDark");
            x1.getStyleClass().add("chatList");
        }

        for (int i = 0; i < settingMid.getChildren().size(); i++) {
            VBox v1 = (VBox) settingMid.getChildren().get(i);
            v1.getStyleClass().remove("settingBoxDark");
            v1.getStyleClass().add("settingBox");
            Label l1 = (Label) v1.getChildren().getFirst();
            l1.setStyle("-fx-text-fill: black;");
        }

        for (int i = 1; i < importMid.getChildren().size(); i++) {
            BorderPane bp = (BorderPane) importMid.getChildren().get(i);
            bp.getStyleClass().remove("chatLogBoxDark");
            bp.getStyleClass().add("chatLogBox");
            bp.getLeft().setStyle("-fx-text-fill: black");
        }

        if (importMid.getChildren().getFirst() instanceof Label) {
            importMid.getChildren().getFirst().setStyle("-fx-text-fill: black;");
        }

        filesBox.setStyle("-fx-background-color: white;");
        File folder = new File("F:/Programs/My Projects/myChatApp/src/main/resources/blackIconFolder");
        File[] files = folder.listFiles();
        if (!filesBox.getChildren().isEmpty() && !receiveFolderClickFlag) {
            for (int i = 0; i < filesBox.getChildren().size(); i++) {
                filesBox.getChildren().get(i).getStyleClass().remove("folderBoxDark");
                filesBox.getChildren().get(i).getStyleClass().add("folderBox");
                Label lbl = (Label) (((VBox) filesBox.getChildren().get(i)).getChildren().getFirst());
                lbl.setTextFill(Color.BLACK);
                ImageView ivw = new ImageView(new Image(files[i].toURI().toString()));
                ivw.setFitWidth(50);
                ivw.setFitHeight(50);
                lbl.setGraphic(ivw);
            }
        } else if (!filesBox.getChildren().isEmpty() && receiveFolderClickFlag) {
            ImageView iview = new ImageView(new Image("arrow-left.png"));
            iview.setFitWidth(28);
            iview.setFitHeight(28);
            ((Label) filesBox.getChildren().getFirst()).setGraphic(iview);
            for (int i = 1; i < filesBox.getChildren().size(); i++) {
                System.out.println("make it bright");
                filesBox.getChildren().get(i).getStyleClass().remove("filesNameBoxDark");
                filesBox.getChildren().get(i).getStyleClass().add("filesNameBox");
                VBox tempBox = (VBox) filesBox.getChildren().get(i);
                ((HBox) tempBox.getChildren().getFirst()).getChildren().getFirst().setStyle("-fx-text-fill: black;");
                ((HBox) tempBox.getChildren().getFirst()).getChildren().getLast().setStyle("-fx-text-fill: black;");
                tempBox.getChildren().get(1).setStyle("-fx-text-fill: black;");
                tempBox.getChildren().getLast().setStyle("-fx-text-fill: black;");
            }
        }
    }

    public void darkMode(VBox midLowerBox, VBox settingMid, VBox importMid, VBox filesBox) {
        if (!serverStatusPopup.getContent().isEmpty()) {
            Label serverStatus = (Label) serverStatusPopup.getContent().getFirst();
            serverStatus.setStyle("-fx-text-fill: white;");
        }

        for (int i = 0; i < vboxList.size(); i++) {
            vboxList.get(i).setStyle("-fx-background-color: #283747");
            StackPane stackPane = (StackPane) vboxList.get(i).getChildren().getFirst();
            ((HBox) stackPane.getChildren().get(1)).getChildren().getFirst().setStyle("-fx-text-fill: white;");
            HBox hb1 = (HBox) ((HBox) stackPane.getChildren().get(1)).getChildren().getLast();
            hb1.getChildren().getLast().setStyle("-fx-text-fill: white");
            stackPane.getChildren().getLast().setStyle("-fx-text-fill: white;");
            ScrollPane sp = (ScrollPane) vboxList.get(i).getChildren().get(1);
            sp.getContent().setStyle("-fx-background-color: #283747");

            VBox x1 = (VBox) midLowerBox.getChildren().get(i);
            HBox x2 = (HBox) x1.getChildren().getFirst();
            HBox x3 = (HBox) x2.getChildren().get(1);
            x3.getChildren().getFirst().setStyle("-fx-text-fill: white;");

            x1.getStyleClass().remove("chatList");
            x1.getStyleClass().add("chatListDark");
        }

        for (int i = 0; i < settingMid.getChildren().size(); i++) {
            VBox v1 = (VBox) settingMid.getChildren().get(i);
//            System.out.println("child: "+v1);
//            System.out.println("style Name: "+v1.getStyleClass());
            v1.getStyleClass().remove("settingBox");
            v1.getStyleClass().add("settingBoxDark");
            Label l1 = (Label) v1.getChildren().getFirst();
            l1.setStyle("-fx-text-fill: white;");
        }

        for (int i = 1; i < importMid.getChildren().size(); i++) {
            BorderPane bp = (BorderPane) importMid.getChildren().get(i);
            bp.getStyleClass().remove("chatLogBox");
            bp.getStyleClass().add("chatLogBoxDark");
            bp.getLeft().setStyle("-fx-text-fill: white");
        }

        if (importMid.getChildren().getFirst() instanceof Label) {
            importMid.getChildren().getFirst().setStyle("-fx-text-fill: white;");
        }

        filesBox.setStyle("-fx-background-color: #1b2631;");
        File folder = new File("F:/Programs/My Projects/myChatApp/src/main/resources/whiteIconFolder");
        File[] files = folder.listFiles();
        if (!filesBox.getChildren().isEmpty() && !receiveFolderClickFlag) {
            for (int i = 0; i < filesBox.getChildren().size(); i++) {
                filesBox.getChildren().get(i).getStyleClass().remove("folderBox");
                filesBox.getChildren().get(i).getStyleClass().add("folderBoxDark");
                Label lbl = (Label) (((VBox) filesBox.getChildren().get(i)).getChildren().getFirst());
                lbl.setTextFill(Color.WHITE);
                ImageView ivw = new ImageView(new Image(files[i].toURI().toString()));
                ivw.setFitWidth(50);
                ivw.setFitHeight(50);
                lbl.setGraphic(ivw);
            }
        } else if (!filesBox.getChildren().isEmpty() && receiveFolderClickFlag) {
            System.out.println("make it dark");
            ImageView iview = new ImageView(new Image("arrow-left-white.png"));
            iview.setFitWidth(28);
            iview.setFitHeight(28);
            ((Label) filesBox.getChildren().getFirst()).setGraphic(iview);
            for (int i = 1; i < filesBox.getChildren().size(); i++) {
                filesBox.getChildren().get(i).getStyleClass().add("filesNameBoxDark");
                VBox tempBox = (VBox) filesBox.getChildren().get(i);
                ((HBox) tempBox.getChildren().getFirst()).getChildren().getFirst().setStyle("-fx-text-fill: white;");
                ((HBox) tempBox.getChildren().getFirst()).getChildren().getLast().setStyle("-fx-text-fill: white;");
                tempBox.getChildren().get(1).setStyle("-fx-text-fill: white;");
                tempBox.getChildren().getLast().setStyle("-fx-text-fill: white;");
            }
        }
    }

    public static String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String[] words = input.toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    public ContextMenu msgOptions(VBox chatArea, HBox msgBox) {
        ContextMenu messageOptions = new ContextMenu();
        Menu deleteMsg = new Menu("Delete");
        MenuItem deleteOption1 = new MenuItem("Delete for me");

        deleteOption1.setOnAction(e -> {
            System.out.println("Click on delete me");
            chatArea.getChildren().remove(msgBox);
        });

        MenuItem deleteOption2 = new MenuItem("Delete for everyone");

        deleteOption2.setOnAction(e -> {
            chatArea.getChildren().remove(msgBox);
            client.sendSignal("SIGNAL:" + rdf.getId(ind) + ":DELETE_" + msgBox.getId());
            clientServer.deleteMsgSignal("SIGNAL:" + rdf.getId(ind) + ":DELETE_" + msgBox.getId());
        });

        deleteMsg.getItems().addAll(deleteOption1, deleteOption2);
        CustomMenuItem langItem = new CustomMenuItem(langList);
        langItem.getStyleClass().add("langItem");
        langItem.setHideOnClick(true);
        Menu translateMenu = new Menu("Translate");
        translateMenu.getItems().add(langItem);

        MenuItem replyItem = new MenuItem("Reply");

        replyItem.setOnAction(e -> {
            String text;
            if (msgBox.getChildren().getFirst() instanceof StackPane bubble) {
                Label msgText = (Label) bubble.getChildren().getFirst();
                text = msgText.getText();
                BorderPane popupBorder = (BorderPane) popupForReply.getContent().getFirst();
                Label popupLabel = (Label) popupBorder.getLeft();
                if (text.contains("\n")) {
                    popupLabel.setText("Reply: " + text.substring(text.indexOf("\n") + 1));
                } else {
                    popupLabel.setText("Reply: " + msgText.getText());
                }
            } else {
                StackPane bubble = (StackPane) msgBox.getChildren().getLast();
                Label msgText = (Label) bubble.getChildren().getFirst();
                text = msgText.getText();
                BorderPane popupBorder = (BorderPane) popupForReply.getContent().getFirst();
                Label popupLabel = (Label) popupBorder.getLeft();
                if (text.contains("\n")) {
                    popupLabel.setText("Reply: " + text.substring(text.indexOf("\n") + 1));
                } else {
                    popupLabel.setText("Reply: " + msgText.getText());
                }
            }
            System.out.println("click on reply");
            popupForReply.show(chatArea, 923, 650);

            offsetX = 923 - commanStage.getX();
            offsetY = 650 - commanStage.getY();

            commanStage.xProperty().addListener((obs, oldX, newX) -> {
                popupForReply.setX(newX.doubleValue() + offsetX);

            });

            commanStage.yProperty().addListener((obs, oldY, newY) -> {
                popupForReply.setY(newY.doubleValue() + offsetY);
            });
        });

        messageOptions.getItems().addAll(deleteMsg, translateMenu, replyItem);

        return messageOptions;
    }

    public ContextMenu fileMsgOptions(double fileSize, String fileName, String correctName, String mobile, ProgressBar progressBar, VBox vBox) {
        ContextMenu fileMessageOptions = new ContextMenu();
        MenuItem splitItem = new MenuItem("Split File");
        MenuItem downloadItem = new MenuItem("Complete Download");
        fileMessageOptions.getItems().addAll(splitItem, downloadItem);

        File file = new File("F:/Programs/My Projects/myChatApp/splitRecFiles/" + fileName.trim());

        System.out.println("=== PATH DEBUG ===");
        System.out.println("File Exist: " + file.exists());

        if (file.exists()) {
            splitItem.setDisable(true);
        }

        splitItem.setOnAction(e -> {
//            MRTB -> Message Received in Text Box
            vBox.getChildren().add(progressBar);
            fileSplitInAlert(fileSize, fileName, true);
            if (fileMessageOptions.isShowing()) {
                fileMessageOptions.hide();
            }
        });

        downloadItem.setOnAction(e -> {
            if (splitItem.isDisable()) {
//                client.requestFile("FILE_SPLIT:" + fileName.concat("|MRTB") + ":SPLIT", fileName, fkey);
                System.out.println("Running RP");
                fileDownload.receiveSplit(fileName, "RP");
            } else {
//                client.requestFile("FILE_REQ:" + fileName, fileName, fkey);
                fileDownload.receiveFull(fileName, correctName, progressBar, mobile, vBox);

            }

            if (fileMessageOptions.isShowing()) {
                fileMessageOptions.hide();
            }
        });

        return fileMessageOptions;
    }

    public Circle perfectCircularPhoto(String url, double diameter, boolean isBorder, boolean isUpdate) {
        // Safely load image from resources
        Image image = null;
        try {
            if (!isUpdate) {
                System.out.println("image: " + url);
                if (url.equals("none")) {
                    url = "/profilePicture/pic.png";
                }
                image = new Image(getClass().getResource(url).toExternalForm());
            } else {
                File file = new File(url);
                image = new Image(file.toURI().toString(), false);
            }
        } catch (Exception e) {
            System.out.println("Image loading failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        // Create a circle and apply image fill
        Circle circle = new Circle(diameter / 2);
        circle.setFill(Color.LIGHTGRAY); // optional fill
        if (isBorder) {
            circle.setStroke(Color.web("#5d63dd")); // border color
            circle.setStrokeWidth(2); // border thickness
        }
        circle.setSmooth(true);
        circle.setFill(new ImagePattern(image));
        return circle;
    }

    public void importChatFiles(File i, String id, HBox root, VBox importMid, VBox vboxRight2, Label vboxRight2Label) {
        node = rdf.getSenderReceiver(i);

        BorderPane chatLogBox = new BorderPane();
        chatLogBox.setId(id.substring(0, id.indexOf(".")));
        chatLogBox.getStyleClass().add("chatLogBox");
        chatLogBox.setPadding(new Insets(10));
        Label chatLogLabel = new Label();
        chatLogLabel.getStyleClass().add("chatLogLabel");

        if (isDark) {
            chatLogLabel.setStyle("-fx-text-fill: white");
            chatLogBox.getStyleClass().remove("chatLogBox");
            chatLogBox.getStyleClass().add("chatLogBoxDark");
        }

        if (!node.get(0).get("sender").asText().equals(currentUserName.toLowerCase())) {
            chatLogLabel.setText("Chat log of " + currentUserName + " and " + capitalizeWords(node.get(0).get("sender").asText()));
            chatLogBox.setLeft(chatLogLabel);
        } else {
            chatLogLabel.setText("Chat log of " + currentUserName + " and " + capitalizeWords(node.get(0).get("receiver").asText()));
            chatLogBox.setLeft(chatLogLabel);
        }

        VBox importChatArea = new VBox(10);
        ScrollPane importChatAreaScroll = new ScrollPane(importChatArea);
        importChatAreaScroll.setFitToWidth(true);
        importChatAreaScroll.setFitToHeight(true);
        vboxRight2.setVgrow(importChatAreaScroll, Priority.ALWAYS);

        chatLogBox.setOnMouseClicked(evt -> {
            System.out.println(chatLogBox.getId());
            if (root.getChildren().size() == 6) {
                root.getChildren().removeLast();
                vboxRight2.getChildren().clear();
            }

            if (isDark) {
                importChatArea.setStyle("-fx-background-color: #283747");
                for (int ind = 0; ind < importChatArea.getChildren().size(); ind++) {
                    HBox h1 = (HBox) importChatArea.getChildren().get(ind);
                    if (h1.getChildren().getFirst() instanceof BorderPane) {
                        BorderPane bpane = (BorderPane) h1.getChildren().getFirst();
                        bpane.getCenter().getStyleClass().remove("timeStamp");
                        bpane.getCenter().getStyleClass().add("timeStampDark");
                    } else {
                        BorderPane bpane = (BorderPane) h1.getChildren().getLast();
                        bpane.getCenter().getStyleClass().remove("timeStamp");
                        bpane.getCenter().getStyleClass().add("timeStampDark");
                    }
                }
            } else {
                importChatArea.setStyle("-fx-background-color: #f8f9f9");
            }

            vboxRight2Label.setText(chatLogLabel.getText());
            vboxRight2.getChildren().addAll(vboxRight2Label, importChatAreaScroll);
            root.getChildren().add(vboxRight2);
            System.out.println(root.getChildren());
        });

        importMsg(importChatArea, node);
        importMid.getChildren().add(chatLogBox);
    }

    public void sendPendingMsg(List<String> list, String id, String pkey) {
        System.out.println("LIST: " + list);
        new Thread(() -> {
            int length = list.size();
            int i = 0;
            while (i < length) {
                client.sendWaitingMsg(id, list.get(i), pkey);
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                synchronized (ackLock) {
                    if (ackMsg != null && ackMsg.contains(list.get(i))) {
                        System.out.println("Acknowledgment: " + ackMsg);
                        i++;
                        ackMsg = null;
                    }
                }
            }
        }).start();
    }

    public void arrangePendingMsg(String id, String pKey) {
        List<String> matchingKeys = new ArrayList<>();

        for (Map.Entry<String, String> entry : list.entrySet()) {
            if (entry.getValue().equals(id)) {
                matchingKeys.add(entry.getKey());
            }
        }

        sendPendingMsg(matchingKeys, id, pKey);
    }

    public void signalHandler(String id, String signalCode) {
        String[] code = signalCode.split("_");
        System.out.println(id + " " + signalCode);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootArray = mapper.readTree(new File("src/main/resources/myData.json"));

            int foundIndex = -1;
            for (int i = 0; i < rootArray.size(); i++) {
                JsonNode obj = rootArray.get(i);
                if (obj.has("mobile") && obj.get("mobile").asText().equals(id)) {
                    foundIndex = i;
                    ScrollPane sp = (ScrollPane) vboxList.get(foundIndex).getChildren().get(1);
                    ca = (VBox) sp.getContent();
                    System.out.println(obj.get("mobile"));
                    if (ca.getChildren().size() > 0) {
                        System.out.println("selected index is " + (Integer.parseInt(code[code.length - 1].replace("DELETE_", "")) - 1));
                        ca.getChildren().remove(Integer.parseInt(code[code.length - 1].replace("DELETE_", "")));
                        System.out.println("Message Deleted Successfully");
                        break;
                    } else {
                        System.out.println("Message is already deleted by the user");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void statusHandler(Socket s) {
        int jsonLength = rdf.jsonLength();
        new Thread(() -> {
            while (true) {
                try {
                    if (s != null) {
                        String userId = rdf.getId(iNum);
                        System.out.println("Checking Status of " + userId);
                        clientServer.checkUserStatus(userId);
                        Thread.sleep(5000);
                        iNum++;
                        if (iNum == jsonLength - 1) {
                            iNum = 0;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void statusUpdate(String id, boolean flag) {
        try {
            int jsonLength = rdf.jsonLength();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootArray = mapper.readTree(new File("src/main/resources/myData.json"));

            int foundIndex = -1;
            for (int i = 0; i < jsonLength; i++) {
                JsonNode obj = rootArray.get(i);
                if (obj.has("mobile") && obj.get("mobile").asText().equals(id)) {
                    foundIndex = i;
                    StackPane topPane = (StackPane) vboxList.get(foundIndex).getChildren().getFirst();
                    HBox hb1 = (HBox) ((HBox) topPane.getChildren().get(1)).getChildren().getLast();
                    Circle circleDot = (Circle) hb1.getChildren().getFirst();
                    Label statusText = (Label) hb1.getChildren().getLast();
                    if (flag) {
                        circleDot.setFill(Color.web("#2ecc71"));
                        statusText.setText("online");
                        if (!onlineIdList.contains(id)) {
                            onlineIdList.add(obj.get("mobile").asText());
                            arrangePendingMsg(id, obj.get("public_key").asText());
                        }
                    } else {
                        circleDot.setFill(Color.web("#e74c3c"));
                        statusText.setText("offline");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void msgRoute(String recMsg, String encryption, boolean isTxtMsg) {
        try {
            String id = recMsg.substring(0, recMsg.indexOf(":"));
            String msg = recMsg.substring(recMsg.indexOf(":") + 1);
            String[] msgParts = msg.split(":");
            System.out.println("bot id: " + id);
            System.out.println("bot message: " + msg);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootArray = mapper.readTree(new File("src/main/resources/myData.json"));

            int foundIndex = -1;
            for (int i = 0; i < rootArray.size(); i++) {
                JsonNode obj = rootArray.get(i);
                if (obj.has("mobile") && obj.get("mobile").asText().equals(id)) {
                    String imgPath = obj.get("img_path").asText();
                    foundIndex = i;
//                    System.out.println("Index is "+foundIndex);
//                    Node n = vboxList.get(foundIndex).lookup("#" + String.valueOf(id));
//                    System.out.println("Node: "+n);
                    StackPane stackPane = (StackPane) vboxList.get(foundIndex).getChildren().getFirst();
                    HBox box = (HBox) stackPane.getChildren().get(1);
                    Label label = (Label) box.getChildren().getFirst();
                    String senderName = label.getText();
                    ScrollPane sp = (ScrollPane) vboxList.get(foundIndex).getChildren().get(1);
                    ca = (VBox) sp.getContent();

                    if (ind != foundIndex) {
                        notifyBox.getChildren().get(foundIndex).setStyle("-fx-background-color: #aed6f1;");
                    }

                    if (msg.startsWith("File sent:")) {
                        String timeDate = String.valueOf(new java.sql.Timestamp(System.currentTimeMillis()));
                        w.writeReceivedFilesJson(senderName, Long.parseLong(id), msgParts[1].trim(), msgParts[2].trim(), Double.parseDouble(msgParts[3]), timeDate, msgParts[msgParts.length - 1]);
                    }
                    receiverMsg(ca, msg, id, imgPath, sp, encryption, senderName, isTxtMsg);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receiverMsg(VBox chatArea, String msg, String id, String img, ScrollPane sp, String encryption, String sender, boolean isTxtMsg) {
        System.out.println("REC MSG: " + msg);
        msgCounter += 1;
//        System.out.println("Msg: "+msg);
//        System.out.println("chatarea: "+chatArea);
        HBox msgSide = new HBox(8); // spacing between bubble and avatar
        msgSide.setAlignment(Pos.TOP_LEFT); // TOP_RIGHT for vertical space
        msgSide.getStyleClass().add("msgSide");

//        ImageView smallPic = circularPhoto(img, 40, 20);

        String[] msgParts = msg.split(":");

        System.out.println("Clicked on INDEX: " + ind);

        Label label = new Label(msgParts[msgParts.length - 1]);
        label.getStyleClass().add("msgLabel");
        label.setWrapText(true);               // Allow line wrapping
        label.setMaxWidth(300);                // Set maximum width of message
        label.setTextOverrun(OverrunStyle.CLIP); // Prevent ellipsis
        label.setPrefWidth(Region.USE_COMPUTED_SIZE);
        label.setMinHeight(Region.USE_PREF_SIZE); // Let it grow in height

        StackPane bubble = new StackPane();
        tempBox = bubble;
        bubble.getStyleClass().add("msgBubble");
        bubble.setMaxWidth(320); // Slightly wider than

        ContextMenu msgMenu = msgOptions(chatArea, msgSide);
        Menu firstMenu = (Menu) msgMenu.getItems().getFirst();
        msgMenu.getItems().removeFirst();
        msgMenu.getItems().addFirst(firstMenu.getItems().getFirst());

        int[] click = {0};
        String[] temporaryStorage = new String[1];
        if (isTxtMsg) {
            String labelId = getCurrentTime() + "|" + sender + "|" + readFile.readName().toLowerCase() + "|" + msg + "|text";
            label.setId(labelId);
            bubble.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    ++click[0];
                    if (click[0] == 1) {
                        if (msgMenu.isShowing()) {
                            msgMenu.hide();
                        }
                        temporaryStorage[0] = label.getText();
                        label.setText(encryption);
                    } else {
                        label.setText(temporaryStorage[0]);
                        click[0] = 0;
                    }
                } else if (e.getButton() == MouseButton.SECONDARY) {
                    if (click[0] == 0) {
                        msgMenu.show(bubble, e.getScreenX() + 30, e.getScreenY());
                        langList.setOnMouseClicked(et -> {
                            String selected = langList.getSelectionModel().getSelectedItem();
                            System.out.println(selected);
                            if (!selected.equals("Original")) {
                                Label lb = (Label) bubble.getChildren().getFirst();
                                String translatedTxt = translateRequest.getTranslation(translateRequest.Post(lb.getText(), languages.get(selected)));
                                lb.setText(translatedTxt);
                            } else {
                                Label lb = (Label) bubble.getChildren().getFirst();
                                lb.setText(msg);
                            }
                        });
                    }
                }
            });
        } else {
            String labelId = getCurrentTime() + "|" + sender + "|" + readFile.readName().toLowerCase() + "|" + msg + "|file";
            label.setId(labelId);
        }

        Circle dot1 = new Circle(3, Color.DARKBLUE);
        Circle dot2 = new Circle(3, Color.DARKBLUE);
        Circle dot3 = new Circle(3, Color.DARKBLUE);
        HBox dots = new HBox(5, dot1, dot2, dot3);
        dots.setAlignment(Pos.CENTER);

        if (msg.startsWith("File sent:")) {
//            w.writeReceivedFilesJson();
            System.out.println("Received Code: " + msg);
            label.setText(msgParts[2]);
//            System.out.println("i'm here");
            msgSide.setId(msgParts[msgParts.length - 1]);
//            System.out.println(msgSide.getId());
            VBox progressbarBox = new VBox();
            progressbarBox.setPadding(new Insets(10, 14, 10, 14));
            ProgressBar bar = new ProgressBar(0);
            bar.setId(id);
            bar.setPrefSize(175, 15);
            progressbarBox.getChildren().add(label);
            client.pbar = bar;
            bubble.getChildren().add(progressbarBox);
            bubble.setId(msgParts[1]);
            bubble.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    Node graphic = label.getGraphic();  // <-- this might be null
                    String lid = (graphic != null) ? graphic.getId() : null;
                    if ("split-file".equals(lid)) {
                        if (!bubble.getChildren().contains(progressbarBox)) progressbarBox.getChildren().add(bar);
                        fileDownload.receiveSplit(msgParts[1], "RP");
                    } else {
                        if (!bubble.getChildren().contains(progressbarBox)) progressbarBox.getChildren().add(bar);
//                        client.requestFile("FILE_REQ:" + bubble.getId(), bubble.getId(), msgSide.getId());
                        System.out.println(msgParts[1] + " " + msgParts[2] + " " + id);
                        fileDownload.receiveFull(msgParts[1], msgParts[2], bar, id, progressbarBox);
                    }
                } else if (e.getButton() == MouseButton.SECONDARY) {
                    System.out.println("ID IN PBAR IS " + bar.getId());
                    double sizeInMB = Double.parseDouble(msgParts[3]) / (1024.0 * 1024.0);
                    ContextMenu fileMsgMenu = fileMsgOptions(sizeInMB, bubble.getId(), label.getText(), id, bar, progressbarBox);
                    fileDownload.progressBarMap.put(msgParts[1].trim(), bar);
                    Path path1 = Paths.get("F:/Programs/My Projects/myChatApp/Received/" + label.getText().replace("encrypted_", "").trim());

                    if (Files.exists(path1)) {
                        fileMsgMenu.hide();
                    } else {
                        fileMsgMenu.show(bubble, e.getScreenX() + 30, e.getScreenY());
                    }

                    if (bar.getProgress() == 1) {
                        bar.setProgress(0);
                    }
                }
            });
            msgSide.getChildren().addAll(perfectCircularPhoto(img, 30, false, false), bubble);
        } else if (msg.equals("Bot_Response")) {
            System.out.println("animation code");
            tempRef = dots;
            tempRef2 = bubble;
            bubble.getChildren().add(dots);
            animateDot(dot1, 0);
            animateDot(dot2, 200);
            animateDot(dot3, 400);
//            sp.setVvalue(1.0);
//            System.out.println(sp.getVvalue());
        } else if (msg.contains(":")) {
            label.setText(msg.substring(msg.indexOf(":") + 1));
            tempRef2.getChildren().remove(tempRef);
            tempRef2.getChildren().add(label);
//            bubble = tempRef2;
        } else {
//            bubble.setPadding(new Insets(10, 14, 10, 14));
            bubble.getChildren().add(label);
        }

        if (!msg.contains(":")) {
            msgSide.getChildren().addAll(perfectCircularPhoto(img, 30, false, false), bubble);
        }

        chatArea.getChildren().add(msgSide);
        Platform.runLater(() -> {
            sp.setVvalue(1.0); // this forces scroll AFTER layout is updated
        });
    }

    public void senderMsg(VBox chatArea2, String msg, String encryption, String receiver, boolean isTxtMsg) {
        msgCounter += 1;
        HBox msgSide2 = new HBox(8); // spacing between bubble and avatar
        msgSide2.setId(String.valueOf(msgCounter));
        msgSide2.setAlignment(Pos.TOP_RIGHT); // TOP_RIGHT for vertical space
        msgSide2.getStyleClass().add("msgSide2");

//        ImageView smallPic2 = circularPhoto("/"+mobile.toString()+".jpeg", 40, 20);
        Label label2 = new Label(msg);
        label2.getStyleClass().add("msgLabel2");
        label2.setWrapText(true);               // Allow line wrapping
        label2.setMaxWidth(300);                // Set maximum width of message
        label2.setTextOverrun(OverrunStyle.CLIP); // Prevent ellipsis
        label2.setPrefWidth(Region.USE_COMPUTED_SIZE);
        label2.setMinHeight(Region.USE_PREF_SIZE); // Let it grow in height

        StackPane bubble = new StackPane(label2);
        bubble.setId(encryption);
        bubble.getStyleClass().add("msgBubble2");
        bubble.setPadding(new Insets(10, 14, 10, 14));
        bubble.setMaxWidth(320); // Slightly wider than label

        ContextMenu msgMenu = msgOptions(chatArea2, msgSide2);

        int[] click = {0};
        Label[] selectedLabel = new Label[1];
        String[] temporaryStorage = new String[1];
        if (isTxtMsg) {
            String labelId = getCurrentTime() + "|" + readFile.readName().toLowerCase() + "|" + receiver + "|" + msg + "|text";
            label2.setId(labelId);
            bubble.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    ++click[0];
                    if (click[0] == 1) {
                        if (msgMenu.isShowing()) {
                            msgMenu.hide();
                        }
                        temporaryStorage[0] = label2.getText();
                        label2.setText(encryption);
                    } else {
                        label2.setText(temporaryStorage[0]);
                        click[0] = 0;
                    }
                } else if (e.getButton() == MouseButton.SECONDARY) {
                    selectedLabel[0] = label2;
                    if (click[0] == 0) {
                        msgMenu.show(bubble, e.getScreenX() + 30, e.getScreenY());
                        langList.setOnMouseClicked(et -> {
                            String selected = langList.getSelectionModel().getSelectedItem();
                            System.out.println(selected);
                            Label lb = (Label) bubble.getChildren().getFirst();
                            if (!selected.equals("Original")) {
                                String translatedTxt = translateRequest.getTranslation(translateRequest.Post(lb.getText(), languages.get(selected)));
                                lb.setText(translatedTxt);
                            } else {
                                lb.setText(msg);
                            }
                        });
                    }
                }
            });
        } else {
            String labelId = getCurrentTime() + "|" + readFile.readName().toLowerCase() + "|" + receiver + "|" + msg + "|file";
            label2.setId(labelId);
        }

        msgSide2.getChildren().addAll(bubble, perfectCircularPhoto("/profilePicture/pic.png", 30, false, false));
//        System.out.println("size: "+chatArea2.getChildren().size());
        chatArea2.getChildren().add(msgSide2);
    }

    public void importMsg(VBox chatArea, ArrayNode arrayNode) {
        try {
            for (JsonNode node : arrayNode) {
                HBox msgSide2 = new HBox(5); // spacing between bubble and avatar

                String message = msgEncodeDecode.symmetricDecrypt(node.get("encrypted").asText());

                Label label = new Label(message);
                label.getStyleClass().add("msgLabel2");
                label.setWrapText(true);               // Allow line wrapping
                label.setMaxWidth(300);                // Set maximum width of message
                label.setTextOverrun(OverrunStyle.CLIP); // Prevent ellipsis
                label.setPrefWidth(Region.USE_COMPUTED_SIZE);
                label.setMinHeight(Region.USE_PREF_SIZE); // Let it grow in height

                String[] formattedTime = node.get("timestamp").asText().split(" ");
                Label timeStamp = new Label(formattedTime[1].substring(0, 5));
                timeStamp.setAlignment(Pos.CENTER);
                timeStamp.getStyleClass().add("timeStamp");
                BorderPane timeStampBox = new BorderPane();
                timeStampBox.setCenter(timeStamp);

                StackPane bubble = new StackPane(label);
                bubble.setPadding(new Insets(10, 14, 10, 14));
                bubble.setMaxWidth(320); // Slightly wider than label

                if (node.get("sender").asText().equals(currentUserName.toLowerCase())) {
                    System.out.println("Right-side");
                    bubble.getStyleClass().add("msgBubble2");
                    msgSide2.setAlignment(Pos.TOP_RIGHT);
                    msgSide2.setPadding(new Insets(0, 10, 0, 0));
                    msgSide2.getChildren().addAll(bubble, timeStampBox);
                } else {
                    System.out.println("Left-side");
                    bubble.getStyleClass().add("msgBubble");
                    msgSide2.setAlignment(Pos.TOP_LEFT);
                    msgSide2.setPadding(new Insets(0, 0, 0, 10));
                    msgSide2.getChildren().addAll(timeStampBox, bubble);
                }

                chatArea.getChildren().add(msgSide2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeImportChatFiles(VBox importMid, String id, VBox vboxRight2) {
        Node nodeToRemove = importMid.lookup("#" + id.substring(0, id.indexOf(".")));
        Label l1 = (Label) (((BorderPane) nodeToRemove).getLeft());
        String s1 = l1.getText();
        if (vboxRight2.getChildren().size() > 0) {
            Label l2 = (Label) vboxRight2.getChildren().getFirst();
            String s2 = l2.getText();
            if (s1.equals(s2)) {
                vboxRight2.getChildren().clear();
            }
        }

        if (nodeToRemove != null) {
            importMid.getChildren().remove(nodeToRemove);
        }
    }

    private Scene signUpPage() {
        HBox root = new HBox();
        VBox vboxLeft = new VBox();
        vboxLeft.getStyleClass().add("vboxLeft");
        VBox vboxRight = new VBox();
        vboxRight.getStyleClass().add("vboxRight");
        root.setHgrow(vboxRight, Priority.ALWAYS);

        Label l1 = new Label("Create your Account");
        l1.getStyleClass().add("l1");

        Label l2 = new Label("Already have Account");
        l2.getStyleClass().add("l2");

        Label l3 = new Label("Login and have fun");
        l3.getStyleClass().add("l3");

        Label warningText = new Label();
        warningText.getStyleClass().add("warningText");

        TextField tf1 = new TextField();
        tf1.setPromptText("Enter your name");
        tf1.setFocusTraversable(false);
        tf1.getStyleClass().add("tf1");

        tf1.focusedProperty().addListener((observable, oldValue, newValue) -> {
            warningText.setText("");
        });

        TextField tf2 = new TextField();
        tf2.setPromptText("Enter your mobile no.");
        tf2.setFocusTraversable(false);
        tf2.getStyleClass().add("tf2");

        UnaryOperator<TextFormatter.Change> positiveIntegerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d{0,10}")) { // Only digits
                return change;
            }
            return null;
        };

        TextFormatter<Integer> textFormatter = new TextFormatter<>(positiveIntegerFilter);
        tf2.setTextFormatter(textFormatter);

        tf2.focusedProperty().addListener((observable, oldValue, newValue) -> {
            warningText.setText("");
        });

        PasswordField tf3 = new PasswordField();
        tf3.setPromptText("Create you Password");
        tf3.setFocusTraversable(false);
        tf3.getStyleClass().add("tf3");

        tf3.focusedProperty().addListener((observable, oldValue, newValue) -> {
            warningText.setText("");
        });

        PasswordField tf4 = new PasswordField();
        tf4.setPromptText("Re-type you Password");
        tf4.setFocusTraversable(false);
        tf4.getStyleClass().add("tf4");

        tf4.focusedProperty().addListener((observable, oldValue, newValue) -> {
            warningText.setText("");
        });

        Button signIn = new Button("Sign In");
        vboxLeft.setMargin(signIn, new Insets(20, 0, 0, 0));
        signIn.setFocusTraversable(false);
        signIn.getStyleClass().add("signIn-btn");

        Button signUp = new Button("Sign Up");
        signUp.setFocusTraversable(false);
        signUp.getStyleClass().add("signIn-btn");
        signUp.getStyleClass().add("signUp-btn");

        Timeline warningMsgTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            warningText.setText("");
        }));

        // Scale down and back up on click
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), signUp);
        scaleDown.setFromX(1.0);
        scaleDown.setFromY(1.0);
        scaleDown.setToX(0.95);
        scaleDown.setToY(0.95);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), signUp);
        scaleUp.setFromX(0.95);
        scaleUp.setFromY(0.95);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);

        ScaleTransition scaleDown2 = new ScaleTransition(Duration.millis(100), signIn);
        scaleDown2.setFromX(1.0);
        scaleDown2.setFromY(1.0);
        scaleDown2.setToX(0.95);
        scaleDown2.setToY(0.95);

        ScaleTransition scaleUp2 = new ScaleTransition(Duration.millis(100), signIn);
        scaleUp2.setFromX(0.95);
        scaleUp2.setFromY(0.95);
        scaleUp2.setToX(1.0);
        scaleUp2.setToY(1.0);

        scaleDown.setOnFinished(e -> scaleUp.play());
        signUp.setOnMousePressed(e -> scaleDown.play());

        scaleDown2.setOnFinished(e -> scaleUp2.play());
        signIn.setOnMousePressed(e -> scaleDown2.play());

        //OTP gui
        BorderPane otpParent = new BorderPane();
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        Label verifyLabel = new Label("Verify your mobile number");
        verifyLabel.getStyleClass().add("verifyLabel");
        Label timeLabel = new Label("OTP valid for: 60 seconds");
        timeLabel.getStyleClass().add("timeLabel");
        Label otpEnterLabel = new Label("Please enter your OTP");
        otpEnterLabel.getStyleClass().add("otpEnterLabel");
        Label resendLabel = new Label("Re-send OTP");
        resendLabel.getStyleClass().add("resendLabel");
        Label infoText = new Label();
        infoText.getStyleClass().add("infoText");
        resendLabel.setCursor(Cursor.HAND);
        HBox otpBox = new HBox(10);
        otpBox.setAlignment(Pos.CENTER);

        Button verifyBtn = new Button("Verify");
        verifyBtn.getStyleClass().add("verifyBtn");

        ScaleTransition scaleDown3 = new ScaleTransition(Duration.millis(100), verifyBtn);
        scaleDown3.setFromX(1.0);
        scaleDown3.setFromY(1.0);
        scaleDown3.setToX(0.95);
        scaleDown3.setToY(0.95);
        ScaleTransition scaleUp3 = new ScaleTransition(Duration.millis(100), verifyBtn);
        scaleUp3.setFromX(0.95);
        scaleUp3.setFromY(0.95);
        scaleUp3.setToX(1.0);
        scaleUp3.setToY(1.0);

        scaleDown3.setOnFinished(e -> scaleUp3.play());
        verifyBtn.setOnMousePressed(e -> scaleDown3.play());

        TextField[] otpFields = new TextField[OTP_LENGTH];

        for (int i = 0; i < OTP_LENGTH; i++) {
            TextField field = new TextField();
            field.getStyleClass().add("field");
            field.setPrefWidth(40);
            field.setAlignment(Pos.CENTER);
            field.setStyle("-fx-font-size: 18px;");

            final int index = i;

            // Only allow 1 digit and move to next
            field.textProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue.length() > 1) {
                    field.setText(newValue.substring(0, 1));
                }
                if (!newValue.matches("\\d?")) {
                    field.setText(oldValue);
                }

                if (!field.getText().isEmpty() && index < OTP_LENGTH - 1) {
                    otpFields[index + 1].requestFocus();
                }
            });

            // Backspace to go back
            field.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case BACK_SPACE:
                        if (field.getText().isEmpty() && index > 0) {
                            otpFields[index - 1].requestFocus();
                        }
                        break;
                    default:
                        break;
                }
            });

            otpFields[i] = field;
            otpBox.getChildren().add(field);
        }

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            --count;
            timeLabel.setText("OTP valid for: " + count + " seconds");
            if (count == 0) {
                box.getChildren().remove(verifyBtn);
                box.getChildren().add(3, resendLabel);
            }
        }));
        timeline.setCycleCount(60);

        Timeline timeline2 = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            infoText.setText("");
        }));

        resendLabel.setOnMouseClicked(e -> {
            timeLabel.setText("OTP valid for: 60 seconds");
            timeline.play();
            box.getChildren().remove(resendLabel);
            box.getChildren().add(3, verifyBtn);
            count = 60;
        });

        verifyBtn.setOnAction(e -> {
            String otpNumber = "";
            for (TextField i : otpFields) {
                if (!i.getText().isEmpty()) {
                    otpNumber = otpNumber.concat(i.getText());
                    i.setText("");
                } else {
                    flag = true;
                }
            }

            if (otpNumber.length() == 4) {
                if (res == 1) {
                    timeline2.play();
                    infoText.setText(responseMsg);
                    int jksResponse = keygen.generateKey(tf4.getText().trim());
                    int aksResponse = keygen.AES_Key_Generator(tf4.getText().trim());
                    if (jksResponse == 1 && aksResponse == 1) {
                        msgEncodeDecode.setUpCode(tf4.getText().trim());
                        try {
                            String public_key = msgEncodeDecode.convertPublicKeyToBase64(msgEncodeDecode.loadPublicKey());
                            dataInsertResponse = db.dataInsert(tf1.getText().trim(), Long.parseLong(tf2.getText().trim()), "yes", tf4.getText().trim(), public_key);
                            if (dataInsertResponse) {
                                w.createJsonFile();
                                w.userInfoCreateFile(tf1.getText().trim(), Long.parseLong(tf2.getText().trim()), "/src/main/resources/pic.png");
                                changeScene(loginPage(), e);
                            } else {
                                vboxRight.getChildren().clear();
                                warningText.setText("Something went wrong");
                                timeline.play();
                                vboxRight.getChildren().addAll(l1, tf1, tf2, tf3, tf4, warningText, signUp);
                                readFile.deleteFile("/AES key/aesKey.jks");
                                readFile.deleteFile("/JKS/keystore.jks");
                            }
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            } else {
                infoText.setText(4 - otpNumber.length() + " digit is missing in OTP");
            }

            //True verification response give data insert in db
        });

        signUp.setOnAction(e -> {
            if (tf1.getText().isEmpty() || tf2.getText().isEmpty() || tf3.getText().isEmpty() || tf4.getText().isEmpty()) {
                warningText.setText("All fields are mandatory to fill");
                warningMsgTimeline.play();
                tempStrStore = "none";
            } else {
                tempStrStore = validation.getValidation(2, tf1.getText(), tf2.getText(), tf3.getText().trim(), tf4.getText().trim());
                warningText.setText(tempStrStore);
                warningMsgTimeline.play();
                validation.returnMsg = "";
            }

            if (tempStrStore == null) {
                if (db.dataExist(tf2.getText()) == 0) {
                    res = GenerateOTP.sendOTP(Long.parseLong(tf2.getText()));
                    vboxRight.getChildren().clear();
                    vboxRight.getChildren().add(otpParent);
                    timeline.play();
                } else {
                    warningText.setText("Mobile number already exist in database");
                }
            }

            tempStrStore = null;
        });

        signIn.setOnAction(e -> changeScene(loginPage(), e));

        box.getChildren().addAll(verifyLabel, timeLabel, otpBox, verifyBtn, infoText);
        otpParent.setCenter(box);

        vboxLeft.setMargin(signUp, new Insets(20, 0, 0, 0));
        vboxLeft.getChildren().addAll(l2, l3, signIn);
        vboxRight.getChildren().addAll(l1, tf1, tf2, tf3, tf4, warningText, signUp);
//        timeline.play();
//        vboxRight.getChildren().add(otpParent);
        root.getChildren().addAll(vboxLeft, vboxRight);

        Scene scene = new Scene(root, appWidth, appHeight);
        scene.getStylesheets().add(getClass().getResource("/signUpStyle.css").toExternalForm());
        return scene;
    }

    private Scene loginPage() {
        HBox root = new HBox();
        VBox vboxLeft = new VBox();
        vboxLeft.getStyleClass().add("vboxLeft");
        VBox vboxRight = new VBox();
        vboxRight.getStyleClass().add("vboxRight");
        root.setHgrow(vboxRight, Priority.ALWAYS);

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            if (text.matches("\\d{0,10}")) {  // only digits allowed
                return change;
            }
            return null;
        };

        TextFormatter<String> textFormatter = new TextFormatter<>(filter);

        Label l1 = new Label("Login to your Account");
        l1.getStyleClass().add("l1");

        Label l2 = new Label("New Here?");
        l2.getStyleClass().add("l2");

        Label l3 = new Label("Sign up and start you chatting journey");
        l3.getStyleClass().add("l3");

        Label l4 = new Label("Enter your Account Mobile Number");
        l4.getStyleClass().add("l4");

        Label forgotPassword = new Label("Forgot Password");
        forgotPassword.getStyleClass().add("forgotPassword");

        Label warningText = new Label();
        warningText.getStyleClass().add("warningText");

        Label timeLabel = new Label("OTP valid for: 60 seconds");
        timeLabel.getStyleClass().add("timeLabel");

        Label resendLabel = new Label("Re-send OTP");
        resendLabel.getStyleClass().add("resendLabel");

        TextField tf1 = new TextField();
        tf1.setTextFormatter(textFormatter);
        tf1.setPromptText("Enter your Mobile");
        tf1.setFocusTraversable(false);
        tf1.getStyleClass().add("tf1");

        PasswordField tf2 = new PasswordField();
        tf2.setPromptText("Enter you Password");
        tf2.setFocusTraversable(false);
        tf2.getStyleClass().add("tf2");

        PasswordField tf3 = new PasswordField();
        tf3.setPromptText("Confirm Password");
        tf3.setFocusTraversable(false);
        tf3.getStyleClass().add("tf2");

        Button signIn = new Button("Sign In");
//            vboxLeft.setMargin(signIn, new Insets(20, 0, 0, 0));
        signIn.setFocusTraversable(false);
        signIn.getStyleClass().add("signIn-btn");

        Button signUp = new Button("Sign Up");
        signUp.setFocusTraversable(false);
        signUp.getStyleClass().add("signIn-btn");
        signUp.getStyleClass().add("signUp-btn");

        // Scale down and back up on click
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), signIn);
        scaleDown.setFromX(1.0);
        scaleDown.setFromY(1.0);
        scaleDown.setToX(0.95);
        scaleDown.setToY(0.95);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), signIn);
        scaleUp.setFromX(0.95);
        scaleUp.setFromY(0.95);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);

        ScaleTransition scaleDown2 = new ScaleTransition(Duration.millis(100), signUp);
        scaleDown2.setFromX(1.0);
        scaleDown2.setFromY(1.0);
        scaleDown2.setToX(0.95);
        scaleDown2.setToY(0.95);

        ScaleTransition scaleUp2 = new ScaleTransition(Duration.millis(100), signUp);
        scaleUp2.setFromX(0.95);
        scaleUp2.setFromY(0.95);
        scaleUp2.setToX(1.0);
        scaleUp2.setToY(1.0);

        scaleDown.setOnFinished(e -> scaleUp.play());
        signIn.setOnMousePressed(e -> scaleDown.play());

        scaleDown2.setOnFinished(e -> scaleUp2.play());
        signUp.setOnMousePressed(e -> scaleDown2.play());

        Button sendOTP = new Button("Send OTP");
        sendOTP.getStyleClass().add("sendOTP");

        ScaleTransition scaleUp3 = new ScaleTransition(Duration.millis(100), sendOTP);
        scaleUp3.setFromX(0.95);
        scaleUp3.setFromY(0.95);
        scaleUp3.setToX(1.0);
        scaleUp3.setToY(1.0);

        ScaleTransition scaleDown3 = new ScaleTransition(Duration.millis(100), sendOTP);
        scaleDown3.setFromX(1.0);
        scaleDown3.setFromY(1.0);
        scaleDown3.setToX(0.95);
        scaleDown3.setToY(0.95);

        scaleDown3.setOnFinished(e -> scaleUp3.play());
        sendOTP.setOnMousePressed(e -> scaleDown3.play());

        Button verifyBtn = new Button("Verify");
        verifyBtn.getStyleClass().add("sendOTP");

        ScaleTransition scaleUp4 = new ScaleTransition(Duration.millis(100), verifyBtn);
        scaleUp4.setFromX(0.95);
        scaleUp4.setFromY(0.95);
        scaleUp4.setToX(1.0);
        scaleUp4.setToY(1.0);

        ScaleTransition scaleDown4 = new ScaleTransition(Duration.millis(100), verifyBtn);
        scaleDown4.setFromX(1.0);
        scaleDown4.setFromY(1.0);
        scaleDown4.setToX(0.95);
        scaleDown4.setToY(0.95);

        scaleDown4.setOnFinished(e -> scaleUp4.play());
        verifyBtn.setOnMousePressed(e -> scaleDown4.play());

        Button passChangeBtn = new Button("Confirm");
        passChangeBtn.getStyleClass().add("sendOTP");

        ScaleTransition scaleUp5 = new ScaleTransition(Duration.millis(100), passChangeBtn);
        scaleUp5.setFromX(0.95);
        scaleUp5.setFromY(0.95);
        scaleUp5.setToX(1.0);
        scaleUp5.setToY(1.0);

        ScaleTransition scaleDown5 = new ScaleTransition(Duration.millis(100), passChangeBtn);
        scaleDown5.setFromX(1.0);
        scaleDown5.setFromY(1.0);
        scaleDown5.setToX(0.95);
        scaleDown5.setToY(0.95);

        scaleDown5.setOnFinished(e -> scaleUp5.play());
        passChangeBtn.setOnMousePressed(e -> scaleDown5.play());

        HBox otpBox = new HBox(10);
        otpBox.setAlignment(Pos.CENTER);

        TextField[] otpFields = new TextField[OTP_LENGTH];

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            --count;
            timeLabel.setText("OTP valid for: " + count + " seconds");
            if (count == 0 && vboxLeft.getChildren().size() == 4) {
                vboxLeft.getChildren().set(3, resendLabel);
            } else if (count == 0 && vboxLeft.getChildren().size() == 5) {
                vboxLeft.getChildren().set(4, resendLabel);
            }
        }));
        timeline.setCycleCount(10);

        for (int i = 0; i < OTP_LENGTH; i++) {
            TextField field = new TextField();
            field.getStyleClass().add("field");
            field.setPrefWidth(40);
            field.setAlignment(Pos.CENTER);
            field.setStyle("-fx-font-size: 18px;");

            final int index = i;

            // Only allow 1 digit and move to next
            field.textProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue.length() > 1) {
                    field.setText(newValue.substring(0, 1));
                }
                if (!newValue.matches("\\d?")) {
                    field.setText(oldValue);
                }

                if (!field.getText().isEmpty() && index < OTP_LENGTH - 1) {
                    otpFields[index + 1].requestFocus();
                }
            });

            // Backspace to go back
            field.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case BACK_SPACE:
                        if (field.getText().isEmpty() && index > 0) {
                            otpFields[index - 1].requestFocus();
                        }
                        break;
                    default:
                        break;
                }
            });

            otpFields[i] = field;
            otpBox.getChildren().add(field);
        }

        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(event -> warningText.setText(""));

        forgotPassword.setOnMouseClicked(e -> {
            System.out.println("clicked on forgot password");
            vboxLeft.getChildren().clear();
            vboxLeft.getChildren().addAll(l4, tf1, sendOTP, warningText);
        });

        resendLabel.setOnMouseClicked(e -> {
            System.out.println("clicked on resend label");
            timeLabel.setText("OTP valid for: 10 seconds");
            timeline.play();
            if (vboxLeft.getChildren().size() == 4) {
                vboxLeft.getChildren().remove(3);
                vboxLeft.getChildren().add(verifyBtn);
            } else {
                vboxLeft.getChildren().remove(4);
                vboxLeft.getChildren().add(verifyBtn);
            }
            count = 10;
        });

        sendOTP.setOnAction(e -> {
            if (tf1.getText().length() < 10) {
                warningText.setText(10 - tf1.getText().trim().length() + "-digits remaining");
                delay.play();
            } else {
                //send otp
                if (db.dataExist(tf1.getText().trim()) == 1) {
                    res = GenerateOTP.sendOTP(Long.parseLong(tf1.getText().trim()));
                    vboxLeft.getChildren().clear();
                    l4.setText("Verify your mobile number");
                    vboxLeft.getChildren().addAll(l4, timeLabel, otpBox, verifyBtn, warningText);
                    timeline.play();
                } else {
                    warningText.setText("No account is exist from this number");
                    delay.play();
                }
            }
        });

        verifyBtn.setOnAction(e -> {
            vboxLeft.getChildren().clear();
            l4.setText("Create your new Password");
            vboxLeft.getChildren().addAll(l4, tf2, tf3, passChangeBtn, warningText);
            String otpNumber = "";
            for (TextField i : otpFields) {
                if (!i.getText().isEmpty()) {
                    otpNumber = otpNumber.concat(i.getText());
                    i.setText("");
                } else {
                    flag = true;
                }
            }

            if (otpNumber.length() == 4) {
                responseMsg = validation.verifyOTP("200");
                delay.play();
                warningText.setText(responseMsg);
                int aesDeleteResponse = readFile.deleteFile("/AES key/aesKey.jks");
                int jksDeleteResponse = readFile.deleteFile("/JKS/keystore.jks");
                if (aesDeleteResponse == 1 && jksDeleteResponse == 1) {
                    vboxLeft.getChildren().clear();
                    l4.setText("Create your new Password");
                    vboxLeft.getChildren().addAll(l4, tf2, tf3, passChangeBtn, warningText);
                }
            } else {
                warningText.setText(4 - otpNumber.length() + " digit is missing in OTP");
            }

            //True verification response give data insert in db
        });

        passChangeBtn.setOnAction(e -> {
            int jksResponse = keygen.generateKey(tf3.getText().trim());
            int aksResponse = keygen.AES_Key_Generator(tf3.getText().trim());
            if (jksResponse == 1 && aksResponse == 1) {
                msgEncodeDecode.setUpCode(tf3.getText().trim());
                try {
                    String public_key = msgEncodeDecode.convertPublicKeyToBase64(msgEncodeDecode.loadPublicKey());
                    int updatePasswordResponse = db.updatePassword(db.hashPassword(tf3.getText()), tf1.getText().trim());
                    int updatePublicKey = db.updatePublicKey(public_key, tf1.getText().trim());
                    if (updatePasswordResponse == 1 && updatePublicKey == 1) {
                        vboxLeft.getChildren().clear();
                        vboxLeft.getChildren().addAll(l1, tf1, tf2, forgotPassword, signIn, warningText);
                    } else {
                        vboxLeft.getChildren().clear();
                        warningText.setText("Something went wrong, try again");
                        delay.play();
                        vboxLeft.getChildren().addAll(l1, tf1, tf2, forgotPassword, signIn, warningText);
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });


        signIn.setOnAction(e -> {
            if (!tf1.getText().trim().isEmpty() && !tf2.getText().trim().isEmpty()) {
                warningText.setText(validation.getValidation(1, tf1.getText().trim(), tf2.getText().trim()));
                delay.play();
                validation.returnMsg = "";

                if (db.dataExist(tf1.getText().trim()) == 1) {
                    db.dataRead(Long.parseLong(tf1.getText().trim()));
                    if (db.checkPassword(tf2.getText().trim(), db.hash)) {
                        mainClient.msg_encode.setUpCode(tf2.getText().trim());
                        changeScene(mainPage(commanStage), e);
                    } else {
                        System.out.println("Something went wrong, please try again");
                    }
                } else {
                    warningText.setText("No account found for this number");
                }
            } else {
                warningText.setText("Enter details for login");
                delay.play();
            }
        });

        signUp.setOnAction(e -> {
            changeScene(signUpPage(), e);
            System.out.println("clicked on it");
        });

        vboxLeft.setMargin(signUp, new Insets(20, 0, 0, 0));
        vboxLeft.getChildren().addAll(l1, tf1, tf2, forgotPassword, signIn, warningText);
//        l4.setText("Create your new Password");
//        vboxLeft.getChildren().addAll(l4, tf2, tf3, passChangeBtn);

        vboxRight.getChildren().addAll(l2, l3, signUp);
        root.getChildren().addAll(vboxLeft, vboxRight);

        Scene loginScene = new Scene(root, appWidth, appHeight);
        loginScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        return loginScene;
    }

    public void chatPersonDelete(VBox root, String targetId, HBox parentBox) {
        ObservableList<Node> children = root.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Node node = children.get(i);
            if (targetId.equals(node.getId())) {
                System.out.println(openMsgBoxId + " " + targetId);
                System.out.println("node deleted");
                root.getChildren().remove(i);
                vboxList.remove(i);
                w.removePerson(targetId);
                if (targetId.equals(openMsgBoxId) && parentBox.getChildren().size() == 4) {
                    parentBox.getChildren().removeLast();
                }
                break;
            }
        }
    }

    public void addPerson(VBox vbox, String uname, String id, String url, ArrayList<VBox> root, HBox hbox, ContextMenu contextMenuItem, boolean runtimeFlag) {
        try {
            VBox chatPerson = new VBox();
            chatPerson.setId(id);
            VBox.setMargin(chatPerson, new Insets(0, 0, 10, 0));
            chatPerson.getStyleClass().add("chatList");

            HBox infoBox = new HBox();
            infoBox.getStyleClass().add("infoBox");

            HBox name = new HBox();
            name.getStyleClass().add("personText");

//            ImageView personPic = circularPhoto(url, 65, 32);
            Label nameText = new Label(uname);
            nameText.getStyleClass().add("nameText");
            HBox threeDotBox = new HBox();
            threeDotBox.setPrefWidth(40);
            threeDotBox.setId(id);
            ImageView threeDot = new ImageView(new Image("/three.png", 20, 20, true, true));
            threeDotBox.getChildren().add(threeDot);
            threeDotBox.setAlignment(Pos.CENTER);

            Menu m1 = (Menu) contextMenuItem.getItems().getLast();

            threeDotBox.setOnMouseClicked(e -> {
//                System.out.println("clicking on three dot");
                if (e.getButton() == MouseButton.PRIMARY) {
                    contextMenuItem.show(threeDotBox, e.getScreenX() + 20, e.getScreenY());
//                    System.out.println(threeDotBox.getId());
                }
                contextMenuItem.getItems().getFirst().setOnAction(event -> {
                    chatPersonDelete(vbox, threeDotBox.getId(), hbox);
                    vbox.getChildren().removeIf(node -> threeDotBox.getId().equals(node.getId()));
                });

                m1.getItems().getFirst().setOnAction(evt -> {
//                    System.out.println("you clicked on the " + vbox.getChildren().indexOf(chatPerson));
                    int foundIndex = vbox.getChildren().indexOf(chatPerson);
                    ScrollPane sp = (ScrollPane) vboxList.get(foundIndex).getChildren().get(1);
                    ca = (VBox) sp.getContent();
                    w.exportJson(ca);
                });

                m1.getItems().getLast().setOnAction(evt -> {
//                    System.out.println("you clicked on the " + m1.getItems().getLast().getText() +" "+threeDotBox.getId());
                    int foundIndex = vbox.getChildren().indexOf(chatPerson);
                    ScrollPane sp = (ScrollPane) vboxList.get(foundIndex).getChildren().get(1);
                    ca = (VBox) sp.getContent();
                    readFile.exportFile(ca);
                });
            });

            name.getChildren().add(nameText);
            infoBox.getChildren().addAll(perfectCircularPhoto(url, 60, false, false), name, threeDotBox);
            chatPerson.getChildren().addAll(infoBox);

            name.setOnMouseClicked(e -> {
                openMsgBoxId = chatPerson.getId();
                if (hbox.getChildren().size() == 6) {
                    botFlag = false;
                    serverStatusPopup.getContent().clear();
                    hbox.getChildren().removeLast();
                    ind = vbox.getChildren().indexOf(chatPerson);
                    hbox.getChildren().add(vboxList.get(vbox.getChildren().indexOf(chatPerson)));
                    System.out.println(hbox.getChildren());
                }

                if (hbox.getChildren().size() == 5) {
                    botFlag = false;
                    ind = vbox.getChildren().indexOf(chatPerson);
                    hbox.getChildren().add(vboxList.get(vbox.getChildren().indexOf(chatPerson)));
                    System.out.println(hbox.getChildren());
                }

//                notifyBox.getChildren().get(ind).setStyle("-fx-background-color: white;");
                System.out.println(vbox.getChildren().get(ind).getId().equals("1111111111"));
                botFlag = vbox.getChildren().get(ind).getId().equals("1111111111");

                if (serverStatus.equals("offline")) {
                    System.out.println("in the server status");
                    Label label = new Label("Server is offline, you can't send message");
                    if (isDark) {
                        label.setStyle("-fx-text-fill: white;");
                    }
                    serverStatusPopup.setAutoHide(false);
                    serverStatusPopup.getContent().add(label);
                    if (!botFlag) {
                        serverStatusPopup.show(name, xserverPopup, yserverPopup);
                    } else {
                        serverStatusPopup.hide();
                    }
                    sOffsetX = xserverPopup - commanStage.getX();
                    sOffsetY = yserverPopup - commanStage.getY();

                    // When window moves, update popup position
                    commanStage.xProperty().addListener((obs, oldX, newX) -> {
                        serverStatusPopup.setX(newX.doubleValue() + sOffsetX);

                    });

                    commanStage.yProperty().addListener((obs, oldY, newY) -> {
                        serverStatusPopup.setY(newY.doubleValue() + sOffsetY);
                    });
                }
            });

            if (runtimeFlag && !vbox.getChildren().contains(chatPerson)) {
                vbox.getChildren().addFirst(chatPerson);
            } else if (!vbox.getChildren().contains(chatPerson)) {
                vbox.getChildren().add(chatPerson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean containsEntry(ListView<CheckBox> listView, String name, long mobile) {
        for (CheckBox cb : listView.getItems()) {
            // Store mobile inside CheckBox using setUserData()
            if (cb.getText().equalsIgnoreCase(name)) {
                return true;
            }
            Long storedMobile = (Long) cb.getUserData();
            if (storedMobile != null && storedMobile == mobile) {
                return true;
            }
        }
        return false;
    }

    public void showReportDialog(Window owner, String reportedName, String reportedId, boolean isGroup) {

        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.initOwner(owner);

        dialog.setTitle("Report " + (isGroup ? "Group" : "Member"));
        dialog.setHeaderText(
                (isGroup ? "Reporting Group" : "Reporting Member") + "\n" +
                        "Name: " + reportedName + "\n" +
                        "ID: " + reportedId
        );

        // Buttons
        ButtonType submitButton = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButton, cancelButton);

        // Reason dropdown
        ComboBox<String> reasonBox = new ComboBox<>();
        reasonBox.getItems().addAll(
                "Harassment / Bullying",
                "Spam / Unwanted Messages",
                "Inappropriate Content",
                "Threats or Safety Concerns",
                "Fake Account / Impersonation",
                "Privacy Violation",
                "Scam / Fraud",
                "Hate Speech",
                "Admin Misuse (Group Only)",
                "Other"
        );
        reasonBox.setPromptText("Select a Reason");

        // Description
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Describe the issue...");
        descriptionArea.setPrefRowCount(4);

        // Attachment area
        Label fileLabel = new Label("No file selected");
        Button attachBtn = new Button("Attach Screenshot");

        attachBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Screenshot");
            File file = chooser.showOpenDialog(owner);
            if (file != null) {
                attachBtn.setUserData(file);
                fileLabel.setText(file.getName());
            }
        });

        // Layout
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Reason:"), 0, 0);
        grid.add(reasonBox, 1, 0);

        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);

        grid.add(new Label("Attachment:"), 0, 2);
        grid.add(attachBtn, 1, 2);
        grid.add(fileLabel, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Convert results to a Map
        dialog.setResultConverter(button -> {
            if (button == submitButton) {
                Map<String, Object> data = new HashMap<>();
                data.put("reason", reasonBox.getValue());
                data.put("description", descriptionArea.getText());
                data.put("attachment", attachBtn.getUserData());
                data.put("reportedName", reportedName);
                data.put("reportedId", reportedId);
                data.put("isGroup", isGroup);
                return data;
            }
            return null;
        });

        // Get the result
        Optional<Map<String, Object>> result = dialog.showAndWait();

        result.ifPresent(data -> {
            System.out.println("----- REPORT SUBMITTED -----");
            System.out.println("Reason: " + data.get("reason"));
            System.out.println("Description: " + data.get("description"));
            System.out.println("Attachment: " + data.get("attachment"));
            System.out.println("Reported Name: " + data.get("reportedName"));
            System.out.println("Reported ID: " + data.get("reportedId"));
        });
    }

    public static void viewReport(
            String reportedName,
            String reportedId,
            String reason,
            String description,
            Image screenshot // nullable
    ) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Report Details");

        Label header = new Label("Reported Member Details");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(new Label(reportedName), 1, 0);

        grid.add(new Label("Member ID:"), 0, 1);
        grid.add(new Label(reportedId), 1, 1);

        grid.add(new Label("Reason:"), 0, 2);
        grid.add(new Label(reason), 1, 2);

        Label descLabel = new Label("Description:");
        TextArea descArea = new TextArea(description);
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefHeight(120);

        // Screenshot button
        Button viewImageBtn = new Button("View Screenshot");
        viewImageBtn.setVisible(screenshot != null);
        viewImageBtn.setManaged(screenshot != null);

        viewImageBtn.setOnAction(e -> openImageWindow(screenshot));

        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(10, viewImageBtn, closeBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(15,
                header,
                grid,
                descLabel,
                descArea,
                buttons
        );
        root.setPadding(new Insets(15));

        dialog.setScene(new Scene(root, 420, 420));
        dialog.showAndWait();
    }

    // Opens image in new window
    private static void openImageWindow(Image image) {
        Stage stage = new Stage();
        stage.setTitle("Screenshot");

        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(800);

        ScrollPane scrollPane = new ScrollPane(imageView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(scrollPane, 900, 600);
        stage.setScene(scene);
        stage.show();
    }

    public ContextMenu groupMenuItemsForMember(String groupId) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem viewMembersItem = new MenuItem("View Members");
        MenuItem leaveGrpItem = new MenuItem("Leave Group");
        MenuItem reportMemberItem = new MenuItem("Report Member");

        viewMembersItem.setOnAction(e -> {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setTitle("Group Member List");

            ListView<String> listView = new ListView<>();

            listView.setCellFactory(lv -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setStyle("-fx-alignment: center;"); // Center-align text
                    }
                }
            });

            for (JsonNode node : rdf.getGroupData()) {
                if (node.get("groupId").asText().equals(groupId)) {
                    listView.getItems().add(capitalizeWords(node.get("groupAdmin").asText().concat(" (Admin)")));
                    if (!node.get("groupAdmin_2").isNull()) listView.getItems().add(capitalizeWords(node.get("groupAdmin_2").asText().concat(" (Admin)")));
                    for (JsonNode n : node.get("memberDetails")) {
                        listView.getItems().add(capitalizeWords(n.get("memberName").asText()));
                    }
                }
            }

            VBox root = new VBox(listView);
            Scene scene = new Scene(root, 250, 300);

            dialog.setScene(scene);
            dialog.showAndWait();
        });

        leaveGrpItem.setOnAction(e -> {
            //TODO: Need to write logic
        });

        reportMemberItem.setOnAction(e -> {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setTitle("Group Member List");

            ListView<CheckBox> listView = new ListView<>();

            for(JsonNode node : rdf.getGroupData()) {
                if (node.get("groupId").asText().equals(groupId)) {
                    for (JsonNode n : node.get("memberDetails")) {
                        CheckBox checkBox = new CheckBox(n.get("memberName").asText());
                        checkBox.setId(n.get("memberId").asText());
                        listView.getItems().add(checkBox);
                    }
                }
            }

            Button addButton = new Button("Report");
            addButton.setMaxWidth(Double.MAX_VALUE);  // full width

            listView.getItems().forEach(cb -> {
                cb.setOnAction(ex -> {
                    if (cb.isSelected()) {
                        // Uncheck all other checkboxes
                        listView.getItems().forEach(other -> {
                            if (other != cb) {
                                other.setSelected(false);
                            }
                        });
                    }
                });
            });


            addButton.setOnAction(ev -> {
                List<CheckBox> selected = listView.getItems()
                        .filtered(CheckBox::isSelected);

                selected.forEach(cb -> {
                    showReportDialog(commanStage, cb.getText(), cb.getId(), false);
                });
            });

            VBox root = new VBox(10, listView, addButton);
            root.setPadding(new Insets(10));
            root.setAlignment(Pos.TOP_CENTER);
            Scene scene = new Scene(root, 250, 300);

            dialog.setScene(scene);
            dialog.showAndWait();
        });

        viewMembersItem.getStyleClass().add("menuItem");
        leaveGrpItem.getStyleClass().add("menuItem");
        reportMemberItem.getStyleClass().add("menuItem");

        contextMenu.getItems().addAll(viewMembersItem, leaveGrpItem, reportMemberItem);

        return contextMenu;

    }

    public ContextMenu groupMenuItems(String groupId, Label groupName, VBox parent) {
        ContextMenu contextMenu = new ContextMenu();
//        contextMenu.getStyleClass().add("contextMenu");

        Menu m1 = new Menu("Group Info");
        MenuItem groupNameItem = new MenuItem("Change Group Name");
        MenuItem groupPhotoItem = new MenuItem("Change Group Photo");

        groupNameItem.getStyleClass().add("menuItem");
        groupPhotoItem.getStyleClass().add("menuItem");

        groupNameItem.setOnAction(e -> {
            groupName.setText(showGroupNameDialog(commanStage));
            GroupJsonGenerator.updateGroupName(groupName.getText(), groupId);
        });

        groupPhotoItem.setOnAction(e -> {
            //TODO: Need to write logic
        });

        m1.getItems().addAll(groupNameItem, groupPhotoItem);

        Menu m2 = new Menu("Members");
        MenuItem viewMembersItem = new MenuItem("View Members");
        MenuItem addMemberItem = new MenuItem("Add Member");
        MenuItem removeMemberItem = new MenuItem("Remove Member");
        MenuItem assignAdminItem = new MenuItem("Assign Admin");
        MenuItem blockMemberItem = new MenuItem("Block Member");
        MenuItem reportMemberItem = new MenuItem("Report Member");

        viewMembersItem.getStyleClass().add("menuItem");
        addMemberItem.getStyleClass().add("menuItem");
        removeMemberItem.getStyleClass().add("menuItem");
        assignAdminItem.getStyleClass().add("menuItem");
        blockMemberItem.getStyleClass().add("menuItem");
        reportMemberItem.getStyleClass().add("menuItem");

        viewMembersItem.setOnAction(e -> {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setTitle("Group Member List");

            ListView<String> listView = new ListView<>();

            listView.setCellFactory(lv -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setStyle("-fx-alignment: center;"); // Center-align text
                    }
                }
            });

            for (JsonNode node : rdf.getGroupData()) {
                if (node.get("groupId").asText().equals(groupId)) {
                    listView.getItems().add(capitalizeWords(node.get("groupAdmin").asText().concat(" (Admin)")));
                    if (!node.get("groupAdmin_2").isNull()) listView.getItems().add(capitalizeWords(node.get("groupAdmin_2").asText().concat(" (Admin)")));
                    for (JsonNode n : node.get("memberDetails")) {
                        listView.getItems().add(capitalizeWords(n.get("memberName").asText()));
                    }
                }
            }

            VBox root = new VBox(listView);
            Scene scene = new Scene(root, 250, 300);

            dialog.setScene(scene);
            dialog.showAndWait();
        });

        addMemberItem.setOnAction(e -> {
            Stage dialog = new Stage();
            dialog.setTitle("Search Person");
            dialog.initModality(Modality.APPLICATION_MODAL);

            // ----- Search Field -----
            TextField searchField = new TextField();
            searchField.setPromptText("Enter Name or Mobile");
            searchField.setStyle("-fx-prompt-text-fill: gray;");

            // ----- ListView with CheckBoxes -----
            ListView<CheckBox> listView = new ListView<>();

            // Filter list on typing

            searchField.textProperty().addListener((obs, oldVal, newVal) -> {

                if (!newVal.isEmpty()) {

//                    listView.getItems().removeIf(cb -> !cb.isDisable());   // remove previous search results

                    ResultSet rs;
                    Long adminMobile = Long.parseLong(rdf.getUserMobile());
                    if (newVal.matches("\\d+")) {
                        rs = db.dataReadByMobile(Long.parseLong(newVal));  // search by number
                    } else {
                        rs = db.dataRead(newVal);  // search by name
                    }

                    ArrayList<Long> memberIdList = new ArrayList<>();
                    memberIdList.add(adminMobile);

                    // Load group members
                    for (JsonNode node : rdf.getGroupData()) {
                        if (node.get("groupId").asText().equals(groupId)) {
                            rdf.getUserMobile();
                            for (JsonNode n : node.get("memberDetails")) {
                                memberIdList.add(n.get("memberId").asLong());
                            }
                        }
                    }

                    try {
                        while (rs.next()) {

                            String uname = rs.getString("uname");
                            long mobile = rs.getLong("mobile");

                            // --- CHECK FOR DUPLICATE (name OR mobile) ---
                            if (containsEntry(listView, uname, mobile)) {
                                continue;
                            }

                            CheckBox checkbox = new CheckBox(uname);
                            checkbox.setUserData(mobile); // store mobile to avoid duplicates

                            if (memberIdList.contains(mobile)) {
                                checkbox.setSelected(true);
                                checkbox.setDisable(true);
                            }

                            listView.getItems().add(checkbox);
                        }

                        rs.close();

                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                else {
                    // --------------------------
                    // CLEAR ONLY SEARCH RESULTS
                    // KEEP:
                    //  ✔ disabled group members
                    //  ✔ manually selected members
                    // --------------------------

                    Platform.runLater(() -> {

                        listView.getItems().removeIf(cb ->
                                !cb.isDisable() &&      // search result
                                        !cb.isSelected()        // not selected
                        );

                        listView.getItems().removeIf(cb ->
                                cb.isDisable() &&      // search result
                                        cb.isSelected()        // not selected
                        );

                    });
                }
            });

            Button addButton = new Button("Add");
            addButton.setMaxWidth(Double.MAX_VALUE);  // full width

            addButton.setOnAction(ev -> {
                System.out.println("Added persons:");
                listView.getItems().forEach(cb -> {
                    if (cb.isSelected()) {
                        GroupJsonGenerator.addMember(groupId, cb.getText(), Long.parseLong(cb.getUserData().toString()));
                    }
                });
                dialog.close();
            });

            // ----- Layout -----
            VBox root = new VBox(10, searchField, listView, addButton);
            root.setPadding(new Insets(10));
            root.setAlignment(Pos.TOP_CENTER);

            dialog.setScene(new Scene(root, 200, 300));
            dialog.showAndWait();
        });

        removeMemberItem.setOnAction(e -> {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setTitle("Group Member List");

            ListView<CheckBox> listView = new ListView<>();

            for(JsonNode node : rdf.getGroupData()) {
                if (node.get("groupId").asText().equals(groupId)) {
                    for (JsonNode n : node.get("memberDetails")) {
                        CheckBox checkBox = new CheckBox(n.get("memberName").asText());
                        checkBox.setId(n.get("memberId").asText());
                        listView.getItems().add(checkBox);
                    }
                }
            }

            Button addButton = new Button("Remove");
            addButton.setMaxWidth(Double.MAX_VALUE);  // full width

            addButton.setOnAction(ev -> {
                List<CheckBox> selected = listView.getItems()
                        .filtered(CheckBox::isSelected);

                selected.forEach(cb ->
                        GroupJsonGenerator.removeMember(groupId, cb.getId())
                );

                listView.getItems().removeAll(selected);
            });

            VBox root = new VBox(10, listView, addButton);
            root.setPadding(new Insets(10));
            root.setAlignment(Pos.TOP_CENTER);
            Scene scene = new Scene(root, 250, 300);

            dialog.setScene(scene);
            dialog.showAndWait();
        });

        assignAdminItem.setOnAction(e -> {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setTitle("Group Member List");

            ListView<CheckBox> listView = new ListView<>();

            for(JsonNode node : rdf.getGroupData()) {
                if (node.get("groupId").asText().equals(groupId)) {
                    for (JsonNode n : node.get("memberDetails")) {
                        CheckBox checkBox = new CheckBox(n.get("memberName").asText());
                        checkBox.setId(n.get("memberId").asText());
                        listView.getItems().add(checkBox);
                    }
                }
            }

            Button addButton = new Button("Create Admin");
            addButton.setMaxWidth(Double.MAX_VALUE);  // full width

            listView.getItems().forEach(cb -> {
                cb.setOnAction(ex -> {
                    if (cb.isSelected()) {
                        // Uncheck all other checkboxes
                        listView.getItems().forEach(other -> {
                            if (other != cb) {
                                other.setSelected(false);
                            }
                        });
                    }
                });
            });


            addButton.setOnAction(ev -> {
                List<CheckBox> selected = listView.getItems()
                        .filtered(CheckBox::isSelected);

                selected.forEach(cb -> {
                    GroupJsonGenerator.addAdmin(groupId, cb.getText(), cb.getId());
                });
                dialog.close();
            });

            VBox root = new VBox(10, listView, addButton);
            root.setPadding(new Insets(10));
            root.setAlignment(Pos.TOP_CENTER);
            Scene scene = new Scene(root, 250, 300);

            dialog.setScene(scene);
            dialog.showAndWait();
        });

        blockMemberItem.setOnAction(e -> {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setTitle("Group Member List");

            ListView<CheckBox> listView = new ListView<>();

            for(JsonNode node : rdf.getGroupData()) {
                if (node.get("groupId").asText().equals(groupId)) {
                    for (JsonNode n : node.get("memberDetails")) {
                        CheckBox checkBox = new CheckBox(n.get("memberName").asText());
                        checkBox.setId(n.get("memberId").asText());
                        listView.getItems().add(checkBox);
                    }
                }
            }

            Button addButton = new Button("Block");
            addButton.setMaxWidth(Double.MAX_VALUE);  // full width

            addButton.setOnAction(ev -> {
                List<CheckBox> selected = listView.getItems()
                        .filtered(CheckBox::isSelected);

                selected.forEach(cb ->
                        System.out.println(cb.getText())
                );
                dialog.close();
            });

            VBox root = new VBox(10, listView, addButton);
            root.setPadding(new Insets(10));
            root.setAlignment(Pos.TOP_CENTER);
            Scene scene = new Scene(root, 250, 300);

            dialog.setScene(scene);
            dialog.showAndWait();
        });

        reportMemberItem.setOnAction(e -> {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setTitle("Group Member List");

            ListView<CheckBox> listView = new ListView<>();

            for(JsonNode node : rdf.getGroupData()) {
                if (node.get("groupId").asText().equals(groupId)) {
                    for (JsonNode n : node.get("memberDetails")) {
                        CheckBox checkBox = new CheckBox(n.get("memberName").asText());
                        checkBox.setId(n.get("memberId").asText());
                        listView.getItems().add(checkBox);
                    }
                }
            }

            Button addButton = new Button("Report");
            addButton.setMaxWidth(Double.MAX_VALUE);  // full width

            listView.getItems().forEach(cb -> {
                cb.setOnAction(ex -> {
                    if (cb.isSelected()) {
                        // Uncheck all other checkboxes
                        listView.getItems().forEach(other -> {
                            if (other != cb) {
                                other.setSelected(false);
                            }
                        });
                    }
                });
            });


            addButton.setOnAction(ev -> {
                List<CheckBox> selected = listView.getItems()
                        .filtered(CheckBox::isSelected);

                selected.forEach(cb -> {
                    showReportDialog(commanStage, cb.getText(), cb.getId(), false);
                });
                dialog.close();
            });

            VBox root = new VBox(10, listView, addButton);
            root.setPadding(new Insets(10));
            root.setAlignment(Pos.TOP_CENTER);
            Scene scene = new Scene(root, 250, 300);

            dialog.setScene(scene);
            dialog.showAndWait();
        });

        m2.getItems().addAll(viewMembersItem, addMemberItem, removeMemberItem,
                assignAdminItem, blockMemberItem, reportMemberItem);

        Menu m3 = new Menu("Permissions");
        CheckMenuItem restrictMsgItem = new CheckMenuItem("Restrict Messaging");
        CheckMenuItem restrictFileItem = new CheckMenuItem("Restrict File Sharing");

        restrictMsgItem.getStyleClass().add("menuItem");
        restrictFileItem.getStyleClass().add("menuItem");

        restrictMsgItem.setOnAction(e -> {
            //TODO: Need to write logic
        });

        restrictFileItem.setOnAction(e -> {
            //TODO: Need to write logic
        });

        m3.getItems().addAll(restrictMsgItem, restrictFileItem);

        Menu m4 = new Menu("Moderation");
        MenuItem blockMemberMsgItem = new MenuItem("Restrict Member Message");
        MenuItem reportItem = new MenuItem("Review reports");

        blockMemberMsgItem.getStyleClass().add("menuItem");
        reportItem.getStyleClass().add("menuItem");

        blockMemberMsgItem.setOnAction(e -> {
            //TODO: Need to write logic
        });

        reportItem.setOnAction(e -> {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.setTitle("Report List");

            ListView<String> listView = new ListView<>();
            listView.getItems().add("1. Tanya reported Mohit");
            listView.getItems().add("2. Tanya reported Mohit");
            listView.getItems().add("3. Tanya reported Mohit");
            listView.getItems().add("4. Tanya reported Mohit");

            listView.setCellFactory(lv -> {
                ListCell<String> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? null : item);
                    }
                };

                cell.setOnMouseClicked(event -> {
                    if (!cell.isEmpty()) {
                        String clickedItem = cell.getItem();
                        System.out.println("Clicked: " + clickedItem);

                        if (event.getClickCount() == 2) {
                            System.out.println("Double-clicked: " + clickedItem);
//                            viewReport("Mohit", "1234567890", "Inappropriate Message", "Using vulger message in group and delete it", new Image("/ai.png"));
                        }
                    }
                });

                return cell;
            });

            VBox root = new VBox(listView);
            Scene scene = new Scene(root, 250, 300);

            dialog.setScene(scene);
            dialog.showAndWait();
        });

        m4.getItems().addAll(blockMemberMsgItem, reportItem);

        Menu m5 = new Menu("More Options");
        MenuItem leaveGrpItem = new MenuItem("Leave Group");
        MenuItem deleteGrpItem = new MenuItem("Delete Group");

        leaveGrpItem.getStyleClass().add("menuItem");
        deleteGrpItem.getStyleClass().add("menuItem");

        leaveGrpItem.setOnAction(e -> {
            //TODO: Need to write logic
        });

        deleteGrpItem.setOnAction(e -> {
            parent.getChildren().remove(parent.lookup("#"+groupId));
            GroupJsonGenerator.deleteGroup(groupId);
        });

        m5.getItems().addAll(leaveGrpItem, deleteGrpItem);

        contextMenu.getItems().addAll(m1, m2, m3, m4, m5);

        for (MenuItem m : contextMenu.getItems()) {
            m.getStyleClass().add("menuItem");
        }

        return contextMenu;
    }

    public void addGroupBox(VBox parent, String id, String groupName) {
        try {
            VBox chatPerson = new VBox();
            chatPerson.setId(id);
            VBox.setMargin(chatPerson, new Insets(0, 0, 10, 0));
            chatPerson.getStyleClass().add("chatList");

            HBox infoBox = new HBox();
            infoBox.getStyleClass().add("infoBox");

            HBox name = new HBox();
            name.getStyleClass().add("personText");

//            ImageView personPic = circularPhoto(url, 65, 32);
            Label nameText = new Label(groupName);
            nameText.getStyleClass().add("nameText");
            HBox threeDotBox = new HBox();
            threeDotBox.setPrefWidth(40);
            threeDotBox.setId(id);
            ImageView threeDot = new ImageView(new Image("/three.png", 20, 20, true, true));
            threeDotBox.getChildren().add(threeDot);
            threeDotBox.setAlignment(Pos.CENTER);

            ContextMenu contextMenu = groupMenuItems(id, nameText, parent);
//            ContextMenu contextMenu = groupMenuItemsForMember(id);
            threeDotBox.setOnMouseClicked(e -> {
                contextMenu.show(threeDotBox, e.getScreenX() + 20, e.getScreenY());
            });

            name.getChildren().add(nameText);
            infoBox.getChildren().addAll(perfectCircularPhoto("none", 60, false, false), name, threeDotBox);
            chatPerson.getChildren().addAll(infoBox);

            Node node = parent;

            while (node != null && !(node instanceof HBox)) {
                node = node.getParent();
            }

            HBox realRoot = (HBox) node;

            name.setOnMouseClicked(e -> {
                System.out.println("ID of this group is " + chatPerson.getId());
                updateChatList(realRoot, groupName, chatPerson.getId(), true);
                realRoot.getChildren().add(vboxList.getFirst());
            });

            parent.getChildren().add(chatPerson);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void chatPerson(VBox vbox, ResultSet r, VBox newbox, ArrayList<VBox> root, HBox hbox, ContextMenu contextMenuItem) {
        try {
            vbox.getChildren().clear();
            Set<String> addedNames = new HashSet<>(); // Track added names
            Set<String> id = new HashSet<>();
            while (r.next()) {
                String uname = r.getString("uname");
                Long mobile = r.getLong("mobile");
                String img_path = r.getString("image_path");
                String public_key = r.getString("public_key");

                // Skip if this uname has already been added
                if (addedNames.contains(uname)) {
                    continue;
                }
                addedNames.add(uname); // Mark as added

                VBox chatPerson = new VBox();
                chatPerson.setId(String.valueOf(mobile));
                VBox.setMargin(chatPerson, new Insets(0, 0, 10, 0));
                chatPerson.getStyleClass().add("chatList");

                HBox infoBox = new HBox();
                infoBox.getStyleClass().add("infoBox");

                VBox name = new VBox();
                name.getStyleClass().add("personText");

//                ImageView personPic = circularPhoto(img_path, 65, 32);
                Label nameText = new Label(uname);
                nameText.getStyleClass().add("nameText");

                if (isDark) {
                    nameText.setStyle("-fx-text-fill: white");
                } else {
                    nameText.setStyle("-fx-text-fill: black");
                }

                StackPane sPane = new StackPane();

                ImageView checkMark = new ImageView(new Image(getClass().getResourceAsStream("/check.png"), 20, 20, true, true));
                checkMark.getStyleClass().add("checkMarkPic");

//                System.out.println(idText.substring(idText.indexOf("%")+1));

                name.getChildren().add(nameText);
                infoBox.getChildren().addAll(perfectCircularPhoto(img_path, 60, false, false), name);
                chatPerson.getChildren().add(infoBox);

                String idText = chatPerson.getId();
//                System.out.println(rdf.searchJson(idText.substring(idText.indexOf("%")+1)));
                if (rdf.searchJson(idText)) {
//                    System.out.println("hello");
                    infoBox.getChildren().add(checkMark);
                }

                chatPerson.setOnMouseClicked((e -> {
                    System.out.println(vbox.getChildren());
                    if (e.getButton() == MouseButton.PRIMARY) {
                        w.write(uname, mobile, img_path, public_key);

                        Node node = (Node) e.getSource();
                        String nodeId = node.getId();

//                     Check if any child in vbox has this ID
                        boolean alreadyExists = newbox.getChildren().stream()
                                .anyMatch(child -> nodeId.equals(child.getId()));

                        if (!id.contains(((Node) e.getSource()).getId())) {
//                        System.out.println(((Node) e.getSource()).getId());
                            id.add(((Node) e.getSource()).getId());

                            if (!infoBox.getChildren().contains(checkMark)) {
                                infoBox.getChildren().add(checkMark);
                            }

                            String str = ((Node) e.getSource()).getId();
                            if (!alreadyExists) {
                                updateChatList(hbox, uname, String.valueOf(mobile), true);
                                addPerson(newbox, uname, String.valueOf(mobile), img_path,
                                        root, hbox, contextMenuItem, true);

                            }
                        } else {
                            id.remove(((Node) e.getSource()).getId());
                            infoBox.getChildren().remove(checkMark);
                            chatPersonDelete(newbox, ((Node) e.getSource()).getId(), hbox);
                        }
                    }
                }));

                vbox.getChildren().add(chatPerson);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ;
        }
    }

    public void updateChatList(HBox root, String uname, String mobile, boolean runtimeFlag) {
        VBox vboxRight = new VBox();
//        vboxRight.setId(mobile);
        vboxRight.getStyleClass().add("vboxRight");
        StackPane personName = new StackPane();
        personName.getStyleClass().add("personName");
        Rectangle rect = new Rectangle(556, 50);
        rect.setArcWidth(20);
        rect.setArcHeight(20);
        rect.setFill(Color.rgb(52, 73, 94, 0.2)); // semi-transparent white
        rect.setEffect(new GaussianBlur(15)); // blur effect
        rect.getStyleClass().add("rect");
        Label nameLabel = new Label(uname);
        nameLabel.getStyleClass().add("name-label");
        Label encryptionLabel = new Label("Messages secure by End-to-End Encryption");
        encryptionLabel.getStyleClass().add("encryptionLabel");
        HBox box = new HBox(5);
        box.setAlignment(Pos.BASELINE_CENTER);
        box.getStyleClass().add("StatusBox");
        Circle circle = new Circle();
        circle.setRadius(3.5);
        circle.setFill(Color.web("#e74c3c"));
        Label label = new Label("offline");
        label.setFont(new Font("Calibre", 14));
        box.getChildren().addAll(circle, label);
        HBox nameAndStatusBox = new HBox(8);
        nameAndStatusBox.setAlignment(Pos.CENTER_LEFT);
        nameAndStatusBox.getChildren().addAll(nameLabel, box);
        personName.getChildren().addAll(rect, nameAndStatusBox, encryptionLabel);
        root.setHgrow(vboxRight, Priority.ALWAYS);

        HBox msgBox = new HBox();
        msgBox.getStyleClass().add("msgBox");
        vboxRight.setVgrow(msgBox, Priority.ALWAYS);
        TextArea msg = new TextArea();
        msg.setPrefRowCount(2);
        msg.setMaxHeight(50); // Prevent growing too tall
        msg.setWrapText(true);
        msg.setPromptText("Type a message...");
        msg.getStyleClass().add("msg-box");
        msg.setFocusTraversable(false);
        Button sendBtn = new Button("Send");
        sendBtn.getStyleClass().add("sendBtn");

        VBox chatArea = new VBox();
//        chatArea.setId(mobile);
        chatArea.getStyleClass().add("chatArea");

        sendBtn.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), sendBtn);
            st.setToX(1.1); // Zoom in 10%
            st.setToY(1.1);
            st.play();
        });

        sendBtn.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), sendBtn);
            st.setToX(1.0); // Return to normal
            st.setToY(1.0);
            st.play();
        });

        // Click press effect (scale down slightly)
        sendBtn.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
//                    if (!progressPopup.isShowing()) {
//                        progressPopup.show(sendBtn, 923, 215);
//                        pOffsetX = 923 - stage.getX();
//                        pOffsetY = 215 - stage.getY();
//
//                        stage.xProperty().addListener((obs, oldX, newX) -> {
//                            progressPopup.setX(newX.doubleValue() + pOffsetX);
//
//                        });
//
//                        stage.yProperty().addListener((obs, oldY, newY) -> {
//                            progressPopup.setY(newY.doubleValue() + pOffsetY);
//                        });
//                    }
//            System.out.println(y);
            if (e.getButton() == MouseButton.PRIMARY) {
                ScaleTransition st = new ScaleTransition(Duration.millis(80), sendBtn);
                st.setToX(0.95);
                st.setToY(0.95);
                st.play();
//                System.out.println(rdf.getId(ind));
                if (serverStatus.equals("online")) {
                    if (isFile) {
//                        System.out.println(selected_File.length());
                        popupForAttachment.hide();
//                        System.out.println("this is the part");
//                        String fullPath = selected_File.getAbsoluteFile().toString().replace("\\", "\\\\");
                        senderMsg(chatArea, "File sent: " + selected_File.getName(), "Not Encrypted", uname, false);
//                        client.sendFile(selected_File, rdf.getId(ind) + ":FILE_OFFER", rdf.getPublicKey(ind));
                        fileUpload.send(9953744795L, rdf.getId(ind), selected_File, rdf.getPublicKey(ind));
                        isFile = false;
                    } else {
//                        System.out.println("below");
                        if (!msg.getText().isEmpty() && !botFlag) {
                            if (popupForReply.isShowing()) {
                                Label popupLabel = (Label) ((BorderPane) popupForReply.getContent().getFirst()).getLeft();
                                tempStr = popupLabel.getText();
                                String msgFormat = "[Reply of " + tempStr.substring(tempStr.indexOf(":") + 2) + "]\n" + msg.getText();
//                                senderEncryption = client.sendMessage(rdf.getId(ind), "[Reply of " + tempStr.substring(tempStr.indexOf(":") + 2) + "]\n" + msg.getText(), rdf.getPublicKey(ind));
                                clientServer.messageSender(rdf.getId(ind), msgFormat, rdf.getPublicKey(ind));
                                senderMsg(chatArea, "[Reply of " + tempStr.substring(tempStr.indexOf(":") + 2) + "]\n" + msg.getText(), senderEncryption, uname, true);
                                popupForReply.hide();
                            } else {
//                                if (label.getText().equals("offline")) {
//                                    list.put(msg.getText(), rdf.getId(ind));
//                                } else {
//                                    senderEncryption = client.sendMessage(rdf.getId(ind), msg.getText(), rdf.getPublicKey(ind));
//                                }
//                                senderEncryption = client.sendMessage(rdf.getId(ind), msg.getText(), rdf.getPublicKey(ind));
                                clientServer.messageSender(rdf.getId(ind), msg.getText().trim(), rdf.getPublicKey(ind));
                                senderMsg(chatArea, msg.getText(), senderEncryption, uname, true);
                            }
//                            System.out.println(rdf.getId(ind) + ":" + msg.getText());
                            msg.setText("");
                        }
                    }
                }

                if (!msg.getText().isEmpty() && botFlag) {
                    senderMsg(chatArea, msg.getText(), "Not Encrypted", uname, false);
                    msgRoute("1111111111" + ":Bot_Response", "Not Encrypted", false);
                    String txt = msg.getText();
                    executors.submit(() -> {
                        botcode.sendRequest(txt, this);
                    });
                    msg.setText("");
                }
            }
        });

        // Click release effect (scale back to hover state or normal)
        sendBtn.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(80), sendBtn);
            // Check if mouse is still hovering
            if (sendBtn.isHover()) {
                st.setToX(1.1);
                st.setToY(1.1);
            } else {
                st.setToX(1.0);
                st.setToY(1.0);
            }
            st.play();
        });

        ImageView attach = new ImageView(new Image(getClass().getResourceAsStream("/link.png")));
        BorderPane attachBox = new BorderPane();
        attachBox.setCenter(attach);
        attach.setFitWidth(32);
        attach.setFitHeight(32);
        attach.setPreserveRatio(true);
        ImageView emoji = new ImageView(new Image(getClass().getResourceAsStream("/emoji.png")));
        BorderPane emojiBox = new BorderPane();
        emojiBox.setCenter(emoji);
        emoji.setFitWidth(35);
        emoji.setFitHeight(35);
        emoji.setPreserveRatio(true);

        emojiBox.setOnMouseClicked(be -> {
            System.out.println("Hello world");
            if (!popupForEmoji.isShowing()) {
                // Show emoji popup near the image view
                popupForEmoji.show(emojiBox,
                        emojiBox.localToScreen(0, emojiBox.getBoundsInParent().getHeight()).getX(),
                        emojiBox.localToScreen(0, emojiBox.getBoundsInParent().getHeight()).getY()
                );
            } else {
                popupForEmoji.hide();
            }
        });

        attachBox.setOnMouseClicked(ae -> {
            if (!popupForAttachment.isShowing()) {
                // Show emoji popup near the image view
                popupForAttachment.show(attachBox,
                        attachBox.localToScreen(0, attachBox.getBoundsInParent().getHeight()).getX() - 220,
                        attachBox.localToScreen(0, attachBox.getBoundsInParent().getHeight()).getY() - 150
                );
            } else {
                popupForAttachment.hide();
            }
        });

        msgBox.getChildren().addAll(msg, sendBtn, attachBox, emojiBox);
        ScrollPane scrollPane1 = new ScrollPane(chatArea);
        scrollPane1.setId(mobile);
        scrollPane1.getStyleClass().add("scrollPane");
        scrollPane1.setFitToWidth(true);

        vboxRight.getChildren().addAll(personName, scrollPane1, msgBox);
        if (runtimeFlag) {
            vboxList.addFirst(vboxRight);
        } else {
            vboxList.add(vboxRight);
        }
    }

    public int fileSplitInAlert(double totalFileSizeMB, String fileName, boolean isMRTB) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText("What is Split File?");

        VBox content = new VBox(8);
        ToggleGroup group = new ToggleGroup();

        Label infoLabel = new Label("Your encrypted file will be split into two parts for added security:\n\n" +
                "Server Storage: One portion (stays until download)\n" +
                "Local Storage: Another portion (on your device)\n\n" +
                "Both portions are required to reconstruct your file.");

        Label headLabel = new Label("Choose Split Method");
        headLabel.setAlignment(Pos.CENTER);
        headLabel.setMaxWidth(Double.MAX_VALUE); // Important: allows label to expand

        RadioButton autoRadio = new RadioButton("Automatic Split (50-50)");
        Label autoSizeLabel = new Label(String.format("Download size: %.2f MB", totalFileSizeMB / 2));
        autoSizeLabel.setPadding(new Insets(0, 0, 0, 20));
        autoRadio.setToggleGroup(group);
        autoRadio.setSelected(true);

        Label autoInfo = new Label(String.format("Server: %.2f MB | Local: %.2f MB",
                totalFileSizeMB / 2, totalFileSizeMB / 2));
        autoInfo.setStyle("-fx-font-size: 11px; -fx-text-fill: gray; -fx-padding: 0 0 0 30;");

        RadioButton manualRadio = new RadioButton("Manual Split (Custom)");
        manualRadio.setToggleGroup(group);

        // Manual split options
        VBox manualOptions = new VBox(10);
        manualOptions.setPadding(new Insets(0, 0, 0, 30));
        manualOptions.setVisible(false);
        manualOptions.setManaged(false);

        HBox sizeBox = new HBox(10);
        Label sizeLabel = new Label("Local storage:");
        TextField sizeField = new TextField();
        sizeField.setPromptText("e.g., 50");
        sizeField.setPrefWidth(100);
        Label mbLabel = new Label("MB");
        sizeBox.getChildren().addAll(sizeLabel, sizeField, mbLabel);

        Label localSizeLabel = new Label(String.format("Server storage: %.2f MB", totalFileSizeMB));
        localSizeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

        // Update local size as user types
        sizeField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                if (!newVal.isEmpty()) {
                    double serverSize = Double.parseDouble(newVal);
                    double localSize = totalFileSizeMB - serverSize;
                    if (localSize >= 0 && serverSize <= totalFileSizeMB) {
                        localSizeLabel.setText(String.format("Server storage: %.2f MB", localSize));
                        localSizeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");
                    } else {
                        localSizeLabel.setText("Invalid size! Must be ≤ " + totalFileSizeMB + " MB");
                        localSizeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: red;");
                    }
                }
            } catch (NumberFormatException e) {
                localSizeLabel.setText("Please enter a valid number");
                localSizeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: red;");
            }
        });

        manualOptions.getChildren().addAll(sizeBox, localSizeLabel);

        autoRadio.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            autoSizeLabel.setVisible(isSelected);
            autoSizeLabel.setManaged(isSelected);
            manualOptions.setVisible(!isSelected);
            manualOptions.setManaged(!isSelected);
            autoInfo.setVisible(!isSelected);
            autoInfo.setManaged(!isSelected);
            alert.getDialogPane().getScene().getWindow().sizeToScene();
        });

        // Show/hide manual options
        manualRadio.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            manualOptions.setVisible(isSelected);
            manualOptions.setManaged(isSelected);
            autoInfo.setVisible(!isSelected);
            autoInfo.setManaged(!isSelected);
            alert.getDialogPane().getScene().getWindow().sizeToScene();
        });

        content.getChildren().addAll(infoLabel, new Separator(), headLabel, autoRadio, autoSizeLabel, manualRadio, manualOptions);

        alert.getDialogPane().setContent(content);

//        Error Alert UI
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Error");
        errorAlert.setHeaderText("Invalid Input");

        // Handle result
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (autoRadio.isSelected()) {
                System.out.println("Automatic is Selected");
//                client.requestFile("FILE_SPLIT:" + fileName + ":HALF");
                if (!isMRTB) {
                    Label lb = fileDownloadUpdateLabel(fileName);
                    fileDownload.alternateReceiver(fileName, "HALF", lb);
                } else {
                    fileDownload.receiveSplit(fileName, "HALF");
                }
                return 1;
            } else {
                String size = sizeField.getText();
                if (size.isEmpty()) {
                    errorAlert.setContentText("Please enter the server storage size");
                    errorAlert.showAndWait();
                    fileSplitInAlert(totalFileSizeMB, fileName, isMRTB); // Show again
                } else {
                    try {
                        double serverSize = Double.parseDouble(size);
                        if (serverSize <= 0 || serverSize > totalFileSizeMB) {
                            errorAlert.setContentText("Server size must be between 0 and " + totalFileSizeMB + " MB");
                            errorAlert.showAndWait();
                            fileSplitInAlert(totalFileSizeMB, fileName, isMRTB); // Show again
                        } else {
                            double localSize = totalFileSizeMB - serverSize;
                            System.out.println("Manual split - Server: " + serverSize + " MB, Local: " + localSize + " MB");
                            long fileLength = (long) (localSize * 1024 * 1024);
//                            client.requestFile("FILE_SPLIT:" + fileName + ":" + fileLength);
                            if (!isMRTB) {
                                Label lb = fileDownloadUpdateLabel(fileName);
                                fileDownload.alternateReceiver(fileName, String.valueOf(fileLength), lb);
                            } else {
                                fileDownload.receiveSplit(fileName, String.valueOf(fileLength));
                            }

                            return 1;
                        }
                    } catch (NumberFormatException ex) {
                        errorAlert.setContentText("Please enter a valid number");
                        errorAlert.showAndWait();
                        fileSplitInAlert(totalFileSizeMB, fileName, isMRTB); // Show again
                    }
                }
            }
        }
        return 0;
    }

    public void fileBoxLoader(File[] filesList, VBox filesBox, Label arrowLabel, String fileType, VBox folderBox1, VBox folderBox2, VBox folderBox3, VBox folderBox4) {
        try {
            File file = new File("src/main/resources/receivedFileJson/output.json");

            // Check file existence FIRST
            if (!file.exists()) {
                System.out.println("File is not present");
                Label lb1 = new Label("Files is not Present");
                BorderPane bp1 = new BorderPane();
                bp1.setPadding(new Insets(200, 0, 0, 0));
                bp1.setCenter(lb1);
                filesBox.getChildren().add(bp1);
                return; // Exit early if file doesn't exist
            }

            arrowLabel.setOnMouseClicked(ev -> {
                receiveFolderClickFlag = false;
                System.out.println("clicked on it");
                filesBox.getChildren().clear();
                filesBox.getChildren().addAll(folderBox1, folderBox2, folderBox3, folderBox4);

                if (isMediaBox) {
                    ((HBox) commanStage.getScene().getRoot()).getChildren().removeLast();
                    musicPlayer.stop();
                    isMediaBox = !isMediaBox;
                }
            });

            // Now safely read the file
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode arrayNode = (ArrayNode) mapper.readTree(file);

            for (JsonNode objNode : arrayNode) {
                if (objNode.has("mobile")) {
                    if (objNode.get("field").isArray()) {
                        for (JsonNode fieldNode : objNode.get("field")) {
                            if (fileType.equals(FileType.fromFileName(fieldNode.get("fileName").asText()).name())) {
                                VBox vb1 = new VBox(0);
                                vb1.getStyleClass().add("filesNameBox");
                                filesUIRef.put(fieldNode.get("fileId").asText(), vb1);
                                HBox hb1 = new HBox(150);
                                hb1.setAlignment(Pos.CENTER);
                                Label lb1 = new Label("From " + objNode.get("name").asText());
                                Label lb2 = new Label(dateModifier(fieldNode.get("receivedTime").asText()));
                                hb1.getChildren().addAll(lb1, lb2);
                                Label lb3 = new Label(fieldNode.get("fileName").asText().replace("encrypted_", ""));
                                ImageView iv1 = new ImageView(new Image("download.png"));
                                iv1.setFitWidth(15);
                                iv1.setFitHeight(15);
                                lb3.setGraphic(iv1);
                                lb3.setAlignment(Pos.CENTER);
                                lb3.setFont(Font.font("Calibri", 16));
                                lb3.setPadding(new Insets(0, 0, 0, 10));
                                HBox hb2 = new HBox();
                                hb2.setAlignment(Pos.CENTER);

                                Hyperlink link = new Hyperlink("Split File");
                                link.setId(objNode.get("mobile").asText());
                                link.setStyle("-fx-border-color: transparent;");
                                link.visitedProperty().addListener((obs, wasVisited, isNowVisited) -> {
                                    if (isNowVisited) link.setVisited(false);
                                });

                                Label lb4 = new Label(calculateRemainingDays(fieldNode.get("receivedTime").asText()) + " days remaining");
                                lb4.setPadding(new Insets(0, 0, 0, 120));
                                hb2.getChildren().addAll(link, lb4);
                                vb1.getChildren().addAll(hb1, lb3, hb2);
                                filesBox.getChildren().addAll(vb1);

                                ImageView downloadingIcon = new ImageView(new Image("downloading.png"));
                                downloadingIcon.setFitWidth(15);
                                downloadingIcon.setFitHeight(15);

                                ImageView tickIcon = new ImageView(new Image("tick.png"));
                                tickIcon.setFitWidth(15);
                                tickIcon.setFitHeight(15);

                                link.setOnAction(e -> {
//                                    System.out.println("Vbox refernce: " + filesUIRef);
                                    if (clientServer.fileExistCheck(fieldNode.get("fileId").asText().trim(), "NONE")) {
                                        double fileLen = rdf.getFileSize(link.getId(), lb3.getText());
                                        double sizeInMB = fileLen / (1024.0 * 1024.0);
                                        String fid = rdf.getFileId(link.getId(), lb3.getText());
                                        if (fileSplitInAlert(sizeInMB, fid, false) == 1) {
                                            link.setText("downloading...");
                                            link.setDisable(true);
                                            link.setOnAction(null);
                                        }
                                    }
//                                    timeline.setCycleCount(10);
//                                    timeline.playFromStart();
                                });

                                if (receiveFolderClickFlag && isDark) {
                                    vb1.getStyleClass().remove("filewNameBox");
                                    vb1.getStyleClass().add("filesNameBoxDark");
                                    ImageView iview = new ImageView(new Image("arrow-left-white.png"));
                                    iview.setFitWidth(28);
                                    iview.setFitHeight(28);
                                    ((Label) filesBox.getChildren().getFirst()).setGraphic(iview);
                                    lb1.setStyle("-fx-text-fill: white;");
                                    lb2.setStyle("-fx-text-fill: white;");
                                    lb3.setStyle("-fx-text-fill: white;");
                                    lb4.setStyle("-fx-text-fill: white;");
                                } else {
                                    vb1.getStyleClass().remove("filewNameBoxDark");
                                    vb1.getStyleClass().add("filesNameBox");
                                    ImageView iview = new ImageView(new Image("arrow-left.png"));
                                    iview.setFitWidth(28);
                                    iview.setFitHeight(28);
                                    ((Label) filesBox.getChildren().getFirst()).setGraphic(iview);
                                    lb1.setStyle("-fx-text-fill: black;");
                                    lb2.setStyle("-fx-text-fill: black;");
                                    lb3.setStyle("-fx-text-fill: black;");
                                    lb4.setStyle("-fx-text-fill: black;");
                                }

                                ImageView finalDownloadingIcon = downloadingIcon;
                                vb1.setOnMouseClicked(e -> {
                                    if (e.getButton() == MouseButton.PRIMARY) {
                                        if (clientServer.fileExistCheck(fieldNode.get("fileId").asText().trim(), "NONE")) {
                                            lb3.setGraphic(finalDownloadingIcon);
                                            link.setText("downloading...");
                                            lb4.setText("0 %");
                                            link.setDisable(true);
                                            link.setOnAction(null);

//                                        client.requestFile("FILE_SPLIT:" + fieldNode.get("fileId").asText() + ":CMPLT");
                                            fileDownload.alternateReceiver(fieldNode.get("fileId").asText(), "CMPLT", lb4);
                                        }
                                    }
                                });

//                                Changes for already downloaded files
                                if (fileNameList.contains(lb3.getText())) {
                                    lb3.setGraphic(tickIcon);
                                    lb4.setText("");
                                    lb4.setPadding(new Insets(0, 0, 0, 180));
                                    link.setText("Open File");
                                    link.setOnAction(null);
                                    vb1.setOnMouseClicked(null);
                                    if (fileType.equals("MUSIC")) {
                                        link.setOnAction(e -> {
                                            playMusic(lb3.getText().replace("encrypted_", ""));
                                        });
                                    } else if (fileType.equals("IMAGE")) {
                                        link.setOnAction(e -> {
                                            imageViewer(lb3.getText().replace("encrypted_", ""));
                                        });
                                    } else if (fileType.equals("DOCUMENT") || fileType.equals("VIDEO")) {
                                        link.setOnAction(e -> {
                                            openFile(new File("./Received/" + lb3.getText().replace("encrypted_", "")));
                                        });
                                    }
                                }

                                if (fieldNode.get("split").asBoolean()) {
                                    iv1.setImage(new Image("fileHalf.png"));
                                    link.setText("Download Remaining");
                                    lb4.setText("");
                                    link.setOnAction(e -> {
                                        fileDownload.alternateReceiver(fieldNode.get("fileId").asText(), "RP", lb4);
                                    });
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Method to show the group name dialog
    private String showGroupNameDialog(Stage owner) {
        Stage dialogStage = new Stage();

        // Variable to hold the result
        final String[] result = {null};

        // Make it modal (blocks interaction with main window)
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(owner);

        // Create UI components
        Label label = new Label("Enter Group Name:");
        TextField groupNameField = new TextField();
        groupNameField.setPromptText("Group name...");
        groupNameField.setMaxWidth(250);

        // Allow only A–Z and a–z
        groupNameField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getText().matches("[a-zA-Z ]*")) {
                return change; // allow
            }
            return null; // block input
        }));

        Button submitButton = new Button("Submit");
        submitButton.setDefaultButton(true); // Makes Enter key trigger this button

        Button cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true); // ESC key closes dialog

        // Handle submit button click
        submitButton.setOnAction(e -> {
            String groupName = groupNameField.getText();
            if (!groupName.trim().isEmpty()) {
                result[0] = groupName;
                dialogStage.close();
            } else {
                label.setText("Please enter a group name!");
                label.setStyle("-fx-text-fill: red;");
            }
        });

        // Handle cancel button click
        cancelButton.setOnAction(e -> {
            result[0] = null;
            dialogStage.close();
        });

        // Layout
        VBox layout = new VBox(15);
        HBox buttonLayout = new HBox(15);
        buttonLayout.setAlignment(Pos.CENTER);
        buttonLayout.getChildren().addAll(submitButton, cancelButton);
        layout.getChildren().addAll(label, groupNameField, buttonLayout);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        // Scene and Stage
        Scene scene = new Scene(layout, 300, 180);
        dialogStage.setTitle("Group Name Dialog");
        dialogStage.setScene(scene);
        dialogStage.setResizable(false);
        dialogStage.showAndWait(); // Waits for dialog to close before returning

        return result[0]; // Returns the group name or null if cancelled
    }

    private HBox copyHBox(String uname, String mobile, VBox parent, BorderPane buttonBox, Map<Long, String> list) {
        HBox name = new HBox();
        name.getStyleClass().add("personText");

        Label nameText = new Label(uname);
        nameText.getStyleClass().add("nameText");

        BorderPane tickBox = new BorderPane();
        tickBox.setPrefWidth(40);
        ImageView checkMark = new ImageView(new Image(getClass().getResourceAsStream("/check.png"), 20, 20, true, true));
        tickBox.setCenter(checkMark);

        HBox containerBox = new HBox(10);
        containerBox.setId(mobile);
        containerBox.setBackground(Background.fill(Color.WHITE));
        containerBox.getStyleClass().add("infoBox2");
        containerBox.getChildren().addAll(perfectCircularPhoto("/profilePicture/pic.png", 60, false, false), name, tickBox);

        containerBox.setOnMouseClicked(e -> {
            if (tickBox.getCenter() instanceof ImageView) {
                tickBox.setCenter(null);
                list.remove(Long.parseLong(containerBox.getId()));
                if (clkCount != 0) clkCount--;
                if (clkCount == 1) parent.getChildren().removeLast();
            } else {
                tickBox.setCenter(checkMark);
                list.put(Long.parseLong(containerBox.getId()), nameText.getText().trim());
                clkCount++;

                if (clkCount > 1) parent.getChildren().add(buttonBox);
            }
        });

        name.getChildren().add(nameText);
        return containerBox;
    }

    private void createCopy(ResultSet r, VBox root, VBox vbox, VBox originalBox, BorderPane buttonBox, ArrayNode arr, Map<Long, String> list) {
        try {
            while (r.next()) {
                HBox name = new HBox();
                name.getStyleClass().add("personText");

                Label nameText = new Label(r.getString("uname").trim());
                nameText.getStyleClass().add("nameText");

                BorderPane tickBox = new BorderPane();
                tickBox.setPrefWidth(40);
                ImageView checkMark = new ImageView(new Image(getClass().getResourceAsStream("/check.png"), 20, 20, true, true));


                HBox containerBox = new HBox(10);
                containerBox.setId(String.valueOf(r.getLong("mobile")));
                containerBox.setBackground(Background.fill(Color.WHITE));
                containerBox.getStyleClass().add("infoBox2");
                containerBox.getChildren().addAll(perfectCircularPhoto("/profilePicture/pic.png", 60, false, false), name, tickBox);

                name.getChildren().add(nameText);
                vbox.getChildren().add(containerBox);

                if (list.containsKey(r.getLong("mobile"))) tickBox.setCenter(checkMark);

                containerBox.setOnMouseClicked(e -> {
                    if (tickBox.getCenter() instanceof ImageView) {
                        tickBox.setCenter(null);
                        originalBox.getChildren().removeLast();
                        list.remove(Long.parseLong(containerBox.getId()));
                        clkCount--;
                    } else {
                        tickBox.setCenter(checkMark);
                        originalBox.getChildren().add(copyHBox(nameText.getText().trim(), containerBox.getId(), root, buttonBox, list));
                        list.put(Long.parseLong(containerBox.getId()), nameText.getText().trim());
                        clkCount++;
                    }
                });
            }

//            Platform.runLater(() -> {
//                if (root.getChildren() instanceof BorderPane) {
//                    root.getChildren().remove(buttonBox);
//                }
//            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public VBox createGroupUI(VBox parent) {
        Map<Long, String> membersList = new HashMap<>();
        ArrayNode arrayNode = rdf.getArrayNode();

        VBox seachbarBox = new VBox();
        seachbarBox.getStyleClass().add("searchbarBox");
        TextField searchBar = new TextField();
        seachbarBox.setMargin(searchBar, new Insets(0, 0, 30, 0));
        searchBar.setPromptText("Search...");
        searchBar.setFocusTraversable(false);
        searchBar.getStyleClass().add("searchBar");
        seachbarBox.getChildren().add(searchBar);

        VBox chatPerson = new VBox();
        chatPerson.setPrefWidth(320);
//        chatPerson.setPrefHeight(549);
        chatPerson.setBackground(Background.fill(Color.WHITE));
        ScrollPane scrollPane = new ScrollPane(chatPerson);
        scrollPane.getStyleClass().add("scrollPane");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox extraVbox = new VBox();
        chatPerson.setPrefWidth(320);
        extraVbox.maxHeightProperty().bind(commanStage.heightProperty());
        ScrollPane tempScrollPane = new ScrollPane(extraVbox);
        tempScrollPane.getStyleClass().add("scrollPane");
        tempScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tempScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
//        chatPerson.setId(id);

        BorderPane buttonBox = new BorderPane();
        Button btn = new Button("Create Group");
        buttonBox.setCenter(btn);
        btn.getStyleClass().add("groupBtn");

        btn.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), btn);
            st.setToX(1.1); // Zoom in 10%
            st.setToY(1.1);
            st.play();
        });

        btn.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), btn);
            st.setToX(1.0); // Return to normal
            st.setToY(1.0);
            st.play();
        });

        btn.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(80), btn);
            // Check if mouse is still hovering
            if (btn.isHover()) {
                st.setToX(1.1);
                st.setToY(1.1);
            } else {
                st.setToX(1.0);
                st.setToY(1.0);
            }
            st.play();
        });

        btn.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            System.out.println("Clicked on the mouse create group button");
            System.out.println("PRESENT MEMBERS ARE: " + membersList);
            String groupName = showGroupNameDialog(commanStage);
            String adminName = rdf.getUserName();
            long groupId = GroupJsonGenerator.createGroup(groupName, adminName, userPassword, membersList);
            if (groupName != null) addGroupBox(parent, String.valueOf(groupId), groupName);
            clientServer.groupInviteSender(String.valueOf(groupId), groupName, adminName, membersList);
            ScaleTransition st = new ScaleTransition(Duration.millis(80), btn);
            st.setToX(0.95);
            st.setToY(0.95);
            st.play();
        });

        VBox root = new VBox();
        root.getChildren().addAll(seachbarBox, scrollPane);
        root.setBackground(Background.fill(Color.WHITE));
        root.setPrefWidth(320);

        searchBar.textProperty().addListener((o, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if (clkCount > 1) {
                    root.getChildren().remove(buttonBox);
                }

                if (root.getChildren().contains(tempScrollPane)) root.getChildren().remove(tempScrollPane);

                extraVbox.getChildren().clear();
                root.getChildren().remove(scrollPane);
                root.getChildren().add(tempScrollPane);


                ResultSet resultSet;
                if (newValue.matches("\\d+")) {
                    resultSet = db.dataReadByMobile(Long.parseLong(newValue));
                } else {
                    resultSet = db.dataRead(newValue);
                }
                createCopy(resultSet, root, extraVbox, chatPerson, buttonBox, arrayNode, membersList);
//                extraVbox.getChildren().clear();
            }

            if (newValue.isEmpty()) {
                extraVbox.getChildren().clear();
                root.getChildren().remove(tempScrollPane);
                root.getChildren().add(scrollPane);
                extraVbox.getChildren().clear();

                if (clkCount > 1 && !(root.getChildren().getLast() instanceof BorderPane)) {
                    root.getChildren().add(buttonBox);
                    if (arrayNode.size() > 6) scrollPane.setPrefHeight(scrollPane.getHeight() - 55);
                }
            }
        });


        for (JsonNode objNode : arrayNode) {
            if (objNode.get("name").asText().equals("chatbot")) continue;
            HBox infoBox = new HBox(10);
            infoBox.setId(objNode.get("mobile").asText().trim());
            infoBox.getStyleClass().add("infoBox2");

            HBox name = new HBox();
            name.setId(objNode.get("name").asText().trim());
            name.getStyleClass().add("personText");

            Label nameText = new Label(objNode.get("name").asText().trim());
            nameText.getStyleClass().add("nameText");

            BorderPane tickBox = new BorderPane();
            tickBox.setPrefWidth(40);
            ImageView checkMark = new ImageView(new Image(getClass().getResourceAsStream("/check.png"), 20, 20, true, true));

            name.getChildren().add(nameText);
            infoBox.getChildren().addAll(perfectCircularPhoto("none", 60, false, false), name, tickBox);
            chatPerson.getChildren().add(infoBox);

            infoBox.setOnMouseClicked(e -> {
                if (tickBox.getCenter() instanceof ImageView) {
                    tickBox.setCenter(null);
                    if (clkCount != 0) clkCount--;
                    membersList.remove(objNode.get("mobile").asLong());
                } else {
                    tickBox.setCenter(checkMark);
                    clkCount++;
                    membersList.put(objNode.get("mobile").asLong(), objNode.get("name").asText().trim());
                }

                if (clkCount == 1) {
                    root.getChildren().remove(buttonBox);
                    if (arrayNode.size() > 6) scrollPane.setPrefHeight(scrollPane.getHeight() + 55);
                    System.out.println(membersList.size());
                }

                if (clkCount > 1 && !(root.getChildren().getLast() instanceof BorderPane)) {
                    root.getChildren().add(buttonBox);
                    if (arrayNode.size() > 6) scrollPane.setPrefHeight(scrollPane.getHeight() - 55);
                }
            });
        }
//        Platform.runLater(() -> System.out.println("scrollpane " + scrollPane.getHeight()));
        return root;
    }

    public Scene mainPage(Stage stage) {
        mainClient.msg_encode.setUpCode(userPassword); // for development purpose !!! this code need to be removed
        mobile = Long.valueOf(rdf.getUserMobile());

        currentUserName = readFile.readName();

        languages.put("Original", "og");
        languages.put("Assamese", "as");
        languages.put("English", "en");
        languages.put("Gujarati", "gu");
        languages.put("Hindi", "hi");
        languages.put("Kannada", "kn");
        languages.put("Malayalam", "ml");
        languages.put("Marathi", "mr");
        languages.put("Nepali", "ne");
        languages.put("Odia", "or");
        languages.put("Punjabi", "pa");
        languages.put("Tamil", "ta");
        languages.put("Telugu", "te");
        languages.put("Urdu", "ur");

        fileReader fr = new fileReader();
        HBox root = new HBox();
        VBox vboxLeft = new VBox();
        vboxLeft.getStyleClass().add("vboxLeft");
        VBox imgContainer = new VBox();
        imgContainer.getStyleClass().add("imgContainer");
        VBox menuContainer = new VBox();
        menuContainer.getStyleClass().add("menuContainer");
        VBox vBoxMid = new VBox();
        vBoxMid.getStyleClass().add("vboxMid");
        VBox midLowerBox = new VBox();
        notifyBox = midLowerBox;
        midLowerBox.getStyleClass().add("midLowerBox");

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getStyleClass().add("contextMenu");
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.getStyleClass().add("menuItem");

        MenuItem changeName = new MenuItem("Rename");
        changeName.getStyleClass().add("menuItem");

        Menu exportChat = new Menu("Export Chat");
        exportChat.getStyleClass().add("menuItem");

        MenuItem textFile = new MenuItem("As Text File");
        textFile.getStyleClass().add("menuItem");
        MenuItem jsonFile = new MenuItem("As JSON File (Recommended)");
        jsonFile.getStyleClass().add("menuItem");
        exportChat.getItems().addAll(jsonFile, textFile);

        contextMenu.getItems().addAll(deleteItem, changeName, exportChat);

        // For Message contextMenu
        langList.getStyleClass().add("langlist");
        langList.setPrefWidth(100);
        langList.setPrefHeight(3 * 24); // Show 3 items only

        languages.forEach((key, value) -> {
            langList.getItems().add(key);
        });

        // Popup for specific message reply
        popupForReply = new Popup();
        popupForReply.setAutoHide(false);
        BorderPane replyBox = new BorderPane();
        replyBox.setPrefSize(575, 35);
        replyBox.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: black; -fx-padding: 10;");
        Label replyLabel = new Label("Reply: How are you");
        replyLabel.setStyle("-fx-font-size: 15; -fx-font-family: calibre");
        replyBox.setLeft(replyLabel);
        popupForReply.getContent().add(replyBox);

        VBox searchResultBox = new VBox();
        searchResultBox.getStyleClass().add("midLowerBox");
//        vBoxMid.setVgrow(midLowerBox, Priority.ALWAYS);

//        deleteBtn.setOnMouseReleased((MouseEvent event) -> {
//            if (event.getButton() == MouseButton.PRIMARY) {
//                holding = false;
//                System.out.println("Mouse released.");
//                // Stop any action that was running during hold
//            }
//        });

        VBox profileNameBox = new VBox();
        profileNameBox.getStyleClass().add("profileNameBox");
        Label profileName = new Label();
        profileName.setText(rdf.getUserName());
        profileName.getStyleClass().add("profileName");
        profileNameBox.getChildren().add(profileName);

        HBox i1 = new HBox();
        HBox i2 = new HBox();
        HBox i3 = new HBox();
        HBox i4 = new HBox();
        HBox i5 = new HBox();
        HBox i6 = new HBox();
        i1.getStyleClass().add("i1");
        i1.getStyleClass().add("menuContainerFirst");
        i2.getStyleClass().add("i2");
        i3.getStyleClass().add("i3");
        i3.setAlignment(Pos.CENTER_LEFT);
        i3.setSpacing(10);
        i4.getStyleClass().add("i4");
        i5.getStyleClass().add("i5");
        i6.getStyleClass().add("i6");

        String profilePic = fr.searchFileName();
        imgContainer.getChildren().add(perfectCircularPhoto("/profilePicture/" + profilePic, 80, true, false));

        Label l1 = new Label("Chat");
        ImageView chat = new ImageView(new Image(getClass().getResourceAsStream("/chat.png")));
        chat.setFitWidth(35);
        chat.setFitHeight(35);
        chat.setPreserveRatio(true);
        l1.setGraphic(chat);
        l1.getStyleClass().add("l1");
        chat.getStyleClass().add("chat-icon");
        i1.getChildren().addAll(l1);

        Label l2 = new Label("Settings");
        ImageView setting = new ImageView(new Image(getClass().getResourceAsStream("/setting.png")));
        setting.setFitWidth(35);
        setting.setFitHeight(35);
        setting.setPreserveRatio(true);
        l2.setGraphic(setting);
        l2.getStyleClass().add("l2");
        i2.getChildren().addAll(l2);

        Label l3 = new Label("Start your Chat");
        l3.getStyleClass().add("l3");

        ImageView brightMode = new ImageView(new Image(getClass().getResourceAsStream("/sun.png")));
        brightMode.setFitWidth(35);
        brightMode.setFitHeight(35);
        brightMode.setPreserveRatio(true);
        ImageView darkMode = new ImageView(new Image(getClass().getResourceAsStream("/moon.png")));
        darkMode.setFitWidth(35);
        darkMode.setFitHeight(35);
        darkMode.setPreserveRatio(true);
        ToggleSwitch toggleSwitch = new ToggleSwitch(40, 20);
        i3.getChildren().addAll(brightMode, toggleSwitch);

        Label l4 = new Label("Import Chat");
        ImageView importIcon = new ImageView(new Image(getClass().getResourceAsStream("/import.png")));
        importIcon.setFitWidth(35);
        importIcon.setFitHeight(35);
        importIcon.setPreserveRatio(true);
        l4.setGraphic(importIcon);
        i4.getChildren().add(l4);
        l4.getStyleClass().add("l4");

        Label l5 = new Label("Received Files");
        ImageView filesIcon = new ImageView(new Image(getClass().getResourceAsStream("/files.png")));
        filesIcon.setFitWidth(35);
        filesIcon.setFitHeight(35);
        filesIcon.setPreserveRatio(true);
        l5.setGraphic(filesIcon);
        l5.getStyleClass().add("l5");
        i5.getChildren().add(l5);

        Label l6 = new Label("Create Group");
        ImageView groupIcon = new ImageView(new Image(getClass().getResourceAsStream("/group.png")));
        groupIcon.setFitWidth(35);
        groupIcon.setFitHeight(35);
        groupIcon.setPreserveRatio(true);
        l6.setGraphic(groupIcon);
        l6.getStyleClass().add("l6");
        i6.getChildren().add(l6);

        VBox seachbarBox = new VBox();
        seachbarBox.getStyleClass().add("searchbarBox");
        TextField searchBar = new TextField();
        seachbarBox.setMargin(searchBar, new Insets(0, 0, 30, 0));
        searchBar.setPromptText("Search...");
        searchBar.setFocusTraversable(false);
        searchBar.getStyleClass().add("searchBar");
        seachbarBox.getChildren().add(searchBar);

        menuContainer.getChildren().addAll(i1, i2, i4, i5, i6, i3);

        String[] chatData = {"Mohit Kumar", "Alice", "Bob", "Charlie"};
//        addPerson(midLowerBox, "Tanishq");

        ScrollPane scrollPane2 = new ScrollPane(midLowerBox);
        scrollPane2.setPrefWidth(320);
//        scrollPane2.setTranslateX(2);
        ScrollPane scrollPane3 = new ScrollPane(searchResultBox);
        scrollPane3.setPrefWidth(320);
        scrollPane2.getStyleClass().add("scrollPane");
        scrollPane3.getStyleClass().add("scrollPane");
//        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane2.setFitToWidth(true);
        scrollPane3.setFitToWidth(true);
        vboxLeft.getChildren().addAll(imgContainer, profileNameBox, menuContainer);
        vBoxMid.getChildren().addAll(seachbarBox, scrollPane2);
//        vBoxMid.setVisible(false);

        FlowPane flowPane = new FlowPane();
        flowPane.setPrefSize(280, 150);
        flowPane.setHgap(10);
        flowPane.setVgap(15);
        Popup emojiPopup = new Popup();
        popupForEmoji = emojiPopup;
        emojiPopup.setAutoHide(true);
        flowPane.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-padding: 10;");
        emojiPopup.getContent().add(flowPane);

        Path dir = Paths.get("F:/Programs/My Projects/myChatApp/src/main/resources/emoji folder"); // Your directory path

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                String fileName = String.valueOf(path.getFileName());
                String unicode = fileName.substring(fileName.indexOf("-") + 1, fileName.indexOf("."));
                int codePoint = Integer.parseInt(unicode, 16);
                char[] chars = Character.toChars(codePoint);
                Label label = new Label();
                label.getStyleClass().add("emojiLabel");
                label.setId("emoji id is " + new String(chars));
                Image img = new Image("/emoji folder/" + fileName);
                ImageView iview = new ImageView(img);
                iview.setFitWidth(30);
                iview.setFitHeight(30);
                label.setGraphic(iview);
                label.setOnMouseClicked(e -> {
                    VBox node1 = (VBox) root.getChildren().getLast();
                    HBox node2 = (HBox) node1.getChildren().getLast();
                    TextArea node3 = (TextArea) node2.getChildren().getFirst();

                    // Append emoji
                    node3.setText(node3.getText() + new String(chars) + " ");

                    // Move caret to end of the text
                    Platform.runLater(() -> node3.positionCaret(node3.getText().length()));

//                    System.out.println(label.getId());
                });


                flowPane.getChildren().add(label);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        HBox attachItemBox = new HBox();
        attachItemBox.setSpacing(10);
        attachItemBox.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-padding: 10; -fx-border-radius: " +
                "20; -fx-background-radius: 20;");
        Popup popup = new Popup();
        popupForAttachment = popup;
        popup.setAutoHide(true);
        popup.getContent().add(attachItemBox);

        FileChooser imgFileChooser = new FileChooser();
        imgFileChooser.setTitle("Choose an Image");
        imgFileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png")
        );

        FileChooser docFileChooser = new FileChooser();
        docFileChooser.setTitle("Choose a Document");
        docFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Document Files", "*.pdf", "*.txt"));

        FileChooser musicFileChooser = new FileChooser();
        musicFileChooser.setTitle("Choose a Music");
        musicFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Music Files", "*.mp3", "*.wav"));

        FileChooser videoFileChooser = new FileChooser();
        videoFileChooser.setTitle("Choose a Video");
        videoFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video Files", "*.mp4"));

        FileChooser[] fileChoosersArray = {docFileChooser, imgFileChooser, musicFileChooser, videoFileChooser};

        BorderPane fileInfoBox = new BorderPane();
        fileInfoBox.setPrefSize(575, 35);
        fileInfoBox.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: black; -fx-padding: 10;");
        Label fileName = new Label();
        fileName.setStyle("-fx-font-size: 15; -fx-font-family: calibre");
        fileInfoBox.setLeft(fileName);
        Popup showFileInfo = new Popup();
        showFileInfo.setAutoHide(true);
        showFileInfo.getContent().add(fileInfoBox);

//        HBox progressBarBox = new HBox();
//        progressBarBox.setSpacing(10);
//        progressBarBox.setAlignment(Pos.CENTER);
//        progressBarBox.setPrefSize(575, 35);
//        progressBarBox.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: black; -fx-padding: 10;");
//        Popup progressPopup = new Popup();
//        progressPopup.getContent().add(progressBarBox);
//        Label progressLabel = new Label("File receiving:");
//        ProgressBar bar = new ProgressBar(0);
//        bar.setPrefSize(300, 15);
//        progressBarBox.getChildren().addAll(progressLabel, bar);
//        progressPopup.setAutoHide(false);

        double docSize = 2.0;
        double imgSize = 2.0;
        double audioSize = 5.0;
        double videoSize = 20.0;

        for (int i = 1; i < 5; i++) {
            Image image = new Image("/attach logo/" + i + ".png");
            ImageView imageView = new ImageView(image);
            imageView.getStyleClass().add("iview");
            imageView.setFitWidth(30);
            imageView.setFitHeight(30);
            imageView.setPreserveRatio(true);
            StackPane stackPane = new StackPane();
            stackPane.setId(String.valueOf(i - 1));
            stackPane.setOnMouseClicked(e -> {
                File selectedFile = fileChoosersArray[Integer.parseInt(stackPane.getId())].showOpenDialog(stage);
                String fName = selectedFile.getName();
                if (selectedFile != null) {
                    isFile = true;
                    selected_File = selectedFile;
                    fileName.setText("Selected File: " + fName);

                    if (fName.matches(".*\\.(pdf|txt)$") && (double) selectedFile.length() / 1048576 > docSize) {
                        fileName.setText("Selected document file size is above 2MB, you can't send it");
                        isFile = false;
                    } else if (fName.matches(".*\\.(jpg|jpeg|png)$") && (double) selectedFile.length() / 1048576 > imgSize) {
                        fileName.setText("Selected image file size is above 2MB, you can't send it");
                        isFile = false;
                    } else if (fName.matches(".*\\.(mp3|wav)$") && (double) selectedFile.length() / 1048576 > audioSize) {
                        fileName.setText("Selected audio file size is above 5MB, you can't send it");
                        isFile = false;
                    } else if (fName.endsWith(".mp4") && (double) selectedFile.length() / 1048576 > videoSize) {
                        fileName.setText("Selected video file size is above 20MB, you can't send it");
                        isFile = false;
                    }

//                    System.out.println(selectedFile.getName());

                    if (!showFileInfo.isShowing()) {
                        showFileInfo.show(stackPane, 923, 650);
                        offsetX = 923 - stage.getX();
                        offsetY = 650 - stage.getY();

                        // When window moves, update popup position
                        stage.xProperty().addListener((obs, oldX, newX) -> {
                            showFileInfo.setX(newX.doubleValue() + offsetX);

                        });

                        stage.yProperty().addListener((obs, oldY, newY) -> {
                            showFileInfo.setY(newY.doubleValue() + offsetY);
                        });
                    } else {
                        showFileInfo.hide();
                    }
                } else {
                    showFileInfo.hide();
                }
            });
            stackPane.getStyleClass().add("circle");
            stackPane.getChildren().add(imageView);
            attachItemBox.getChildren().add(stackPane);
        }

//        int ind = 0;
        File file = new File("src/main/resources/myData.json");
        if (file.length() != 0 && file.exists()) {
            for (JsonNode node : rdf.read()) {
                System.out.println("image path: " + node.get("img_path").asText());
                updateChatList(root, node.get("name").asText(), node.get("mobile").asText(), false);
                addPerson(midLowerBox, capitalizeWords(node.get("name").asText()), node.get("mobile").asText(),
                        node.get("img_path").asText(), vboxList, root, contextMenu, false);
            }
        }

//        For Group Adding
        File groupFile = new File("./GroupJson/groupsData.json");
        if (groupFile.length() != 0 && groupFile.exists()) {
            for (JsonNode node : rdf.getGroupData()) {
                updateChatList(root, node.get("groupName").asText(), node.get("groupId").asText(), false);
                addGroupBox(midLowerBox, capitalizeWords(node.get("groupId").asText()), node.get("groupName").asText());
            }
        }

        // To Switch between vbox and fetch detail from db
        searchBar.textProperty().addListener((o, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                vBoxMid.getChildren().remove(scrollPane2);
                if (!vBoxMid.getChildren().contains(scrollPane3)) {
                    vBoxMid.getChildren().add(scrollPane3);
                }

                ResultSet resultSet = db.dataRead(newValue);

                if (newValue.matches("\\d+")) {
                    resultSet = db.dataReadByMobile(Long.parseLong(newValue));
                }

//                searchResultBox.getChildren().clear();
                chatPerson(searchResultBox, resultSet, midLowerBox, vboxList, root, contextMenu);
            } else {
                vBoxMid.getChildren().remove(scrollPane3);
                if (!vBoxMid.getChildren().contains(scrollPane2)) {
                    vBoxMid.getChildren().add(scrollPane2);
                }
            }
        });


//        String[] a = {"hi", "What are doing?", "Where are you"};
//        for (int j = 0; j < 15; j++) {
//            chatArea.getChildren().add(setTimeline());
//            receiverMsg(chatArea, "What are you doing?");
//            receiverMsg(chatArea, "What are you doing?");
//            senderMsg(chatArea, "Nothing, how about you?");
//            senderMsg(chatArea, "Nothing, how about you?");
//        }

        VBox settingMid = new VBox();
        settingMid.getStyleClass().add("settingMid");

        VBox importMid = new VBox();
        importMid.getStyleClass().add("importMid");
        importMid.setAlignment(Pos.CENTER);
        Label noChatFileLabel = new Label("No Chat File is Present");
        ScrollPane importMidScrollPane = new ScrollPane(importMid);
        importMid.getChildren().add(noChatFileLabel);
        importMidScrollPane.setFitToWidth(true);
        importMidScrollPane.setFitToHeight(true);

        VBox accountNameBox = new VBox();
        accountNameBox.setPadding(new Insets(10));
        accountNameBox.getStyleClass().add("settingBox");
        Label accountName = new Label("Change Account Name");
        accountName.getStyleClass().add("settingLabels");
        accountNameBox.getChildren().add(accountName);

        VBox vboxRight1 = new VBox();
        vboxRight1.getStyleClass().add("vboxRight1");
        root.setHgrow(vboxRight1, Priority.ALWAYS);

        VBox vboxRight2 = new VBox();
        vboxRight2.getStyleClass().add("vboxRight2");
        root.setHgrow(vboxRight2, Priority.ALWAYS);
        Label vboxRight2Label = new Label();
        vboxRight2Label.getStyleClass().add("vboxRight2Label");


        Label heading = new Label();
        heading.getStyleClass().add("settingRightHeading");
        Label warningText = new Label();
        warningText.getStyleClass().add("warningText");
        Label currentName = new Label();
        currentName.getStyleClass().add("currentNameLabel");
        currentName.setText("Current Name: " + fr.readName()); // need to change
        Label updateMsg = new Label();
        updateMsg.getStyleClass().add("updateMsg");
        TextField tf1 = new TextField();
        tf1.setPromptText("Update your Account Name");
        tf1.setFocusTraversable(false);
        tf1.getStyleClass().add("tf1");
        PasswordField pf1 = new PasswordField();
        pf1.setPromptText("Current Password");
        pf1.setFocusTraversable(false);
        pf1.getStyleClass().add("tf1");
        PasswordField pf2 = new PasswordField();
        pf2.setPromptText("New Password");
        pf2.setFocusTraversable(false);
        pf2.getStyleClass().add("tf1");
        PasswordField pf3 = new PasswordField();
        pf3.setPromptText("Confirm Password");
        pf3.setFocusTraversable(false);
        pf3.getStyleClass().add("tf1");
        VBox saveBtnBox = new VBox();
        saveBtnBox.getStyleClass().add("saveBtnBox");
        Button saveBtn = new Button("Save Changes");
        saveBtn.getStyleClass().add("saveBtn");
        saveBtnBox.getChildren().add(saveBtn);

        accountNameBox.setOnMouseClicked(e -> {
            if (root.getChildren().size() == 6) {
                root.getChildren().removeLast();
            }
            System.out.println(accountName.getText());
            vboxRight1.getChildren().clear();
            heading.setText("Account Name");
            vboxRight1.getChildren().addAll(heading, currentName, tf1, saveBtnBox);
            root.getChildren().add(vboxRight1);
            saveBtn.setOnAction(buttonEvent -> {
                int status = db.updateName(tf1.getText().trim().toLowerCase(), mobile.toString());
                if (status == 1) {
                    fr.updateName(tf1.getText().trim());
                    updateMsg.setText("Updated Successfully");
                    currentName.setText("Current Name: " + fr.readName().trim());
                    profileName.setText(fr.readName().trim());
                    tf1.setText("");
                }
            });
        });
//        vboxRight1.getChildren().addAll(heading, warningText, tf1, tf2, tf3, forgotPassword, saveBtnBox);

        VBox photoChangeBox = new VBox();
        photoChangeBox.setPadding(new Insets(10));
        photoChangeBox.getStyleClass().add("settingBox");
        Label photoChange = new Label("Change your Profile Photo");
        photoChange.getStyleClass().add("settingLabels");
        photoChangeBox.getChildren().add(photoChange);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose an Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png")
        );
        File appDirectory = new File("src/main/resources/profilePicture"); // A folder named 'images' in your app directory
        if (!appDirectory.exists()) {
            appDirectory.mkdirs(); // Create it if it doesn't exist
        }

        photoChangeBox.setOnMouseClicked(ev -> {
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                System.out.println("Selected File: " + selectedFile.getName());
                if (db.updateImgPath("/src/main/resources/" + selectedFile.getName(), mobile.toString())) {
                    fr.deleteFile("src/main/resources/profilePicture/" + fr.searchFileName());

                    // Use original file name OR rename it by mobile number
                    File targetFile = new File(appDirectory, selectedFile.getName());

                    try {
                        // Save the selected image to our external profilePicture directory
                        Files.copy(selectedFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Image copied to: " + targetFile.getAbsolutePath());
                        w.updateProfilePicLocation("/src/main/resources/" + targetFile.getName());
                        // ✅ Now refresh the profile picture UI
                        imgContainer.getChildren().clear();
//                    ImageView newpic = updatingCircularPhoto(targetFile.getAbsolutePath(), 80, 40);
                        imgContainer.getChildren().add(perfectCircularPhoto(targetFile.getAbsolutePath(), 80, true, true));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Something went wrong, please try again");
                }
            }
        });

        VBox passwordChangeBox = new VBox();
        passwordChangeBox.setPadding(new Insets(10));
        passwordChangeBox.getStyleClass().add("settingBox");
        Label passwordChange = new Label("Change your Account Password");
        passwordChange.getStyleClass().add("settingLabels");
        passwordChangeBox.getChildren().add(passwordChange);

        passwordChangeBox.setOnMouseClicked(e -> {
//            System.out.println(passwordChange.getText());
            if (root.getChildren().size() == 6) {
                root.getChildren().removeLast();
            }
            vboxRight1.getChildren().clear();
            heading.setText("Account Password");
            vboxRight1.getChildren().addAll(heading, pf1, pf2, pf3, saveBtnBox, warningText);
            root.getChildren().add(vboxRight1);
            saveBtn.setOnAction(buttonEvent -> {
                if (!pf1.getText().isEmpty() && !pf2.getText().isEmpty() && !pf3.getText().isEmpty()) {
                    db.dataRead(mobile);
                    if (db.checkPassword(pf1.getText().trim(), db.hash)) {
                        if (pf2.getText().length() > 5) {
                            if (pf2.getText().equals(pf3.getText())) {
                                keyGen.changeJksPass(pf1.getText().trim(), pf3.getText().trim());
                                db.updatePassword(db.hashPassword(pf3.getText().trim()), mobile.toString());
                                updateMsg.setText("Password updated Successfully");
                                vboxRight1.getChildren().add(updateMsg);
                            } else {
                                warningText.setText("Password is not same");
                            }
                        } else {
                            warningText.setText("Minimum Password length is 6");
                        }
                    } else {
                        System.out.println("current password is not correct");
                    }
                } else {
                    warningText.setText("All fields are required to fill");
                }

            });
        });

        VBox deleteAccountBox = new VBox();
        deleteAccountBox.setPadding(new Insets(10));
        deleteAccountBox.getStyleClass().add("settingBox");
        Label deleteAccount = new Label("Delete your Account");
        deleteAccount.getStyleClass().add("settingLabels");
        deleteAccountBox.getChildren().add(deleteAccount);

        deleteAccountBox.setOnMouseClicked(e -> {
            vboxRight1.getChildren().clear();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Are you sure you want to delete?");

            ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType delete = new ButtonType("Delete");

            alert.getButtonTypes().setAll(cancel, delete);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == delete) {
                System.out.println("clicked on the delete button");
                String file_name = fr.searchFileName();
                int dbRemoveFlag = db.removeData(mobile);
                int picDeleteFlag = fr.deleteFile("src/main/resources/profilePicture/" + file_name);
                int deleteKeyFlag = fr.deleteFile("src/main/resources/JKS/keystore.jks");
                int deleteJsonFlag = fr.deleteFile("src/main/resources/myData.json");
                int deleteAccountFile = fr.deleteFile("src/main/resources/accountInfo.txt");
                if (dbRemoveFlag == 1 && deleteKeyFlag == 1 && deleteJsonFlag == 1 && deleteAccountFile == 1 && picDeleteFlag == 1) {
                    commanStage.setScene(loginPage());
                } else {
                    System.out.println("Something went wrong");
                }

            } else {
                System.out.println("Deletion cancelled");
            }
        });

        vboxRight1.getChildren().add(updateMsg);

        settingMid.setVisible(false);
        settingMid.setManaged(false);
//        importMidScrollPane.setVisible(false);
        importMidScrollPane.setManaged(false);

        //Received Files Option
        VBox filesBox = new VBox();
        filesBox.getStyleClass().add("filesBox");
        ScrollPane filesBoxScrollPane = new ScrollPane(filesBox);
        filesBoxScrollPane.setFitToHeight(true);
        filesBoxScrollPane.setFitToWidth(true);
        filesBoxScrollPane.setManaged(false);
        filesBoxScrollPane.setVisible(false);

        ImageView imageView1 = new ImageView(new Image(getClass().getResource("/blackIconFolder/folderDoc.png").toExternalForm()));
        imageView1.setFitWidth(50);
        imageView1.setFitHeight(50);
        Label docLabel = new Label("Documents");
        docLabel.setFont(Font.font("Calibri", 20));
        docLabel.setGraphic(imageView1);
        VBox folderBox1 = new VBox(docLabel);
        folderBox1.getStyleClass().add("folderBox");

        ImageView imageView2 = new ImageView(new Image(getClass().getResource("/blackIconFolder/folderImg.png").toExternalForm()));
        imageView2.setFitWidth(50);
        imageView2.setFitHeight(50);
        Label imgLabel = new Label("Images");
        imgLabel.setFont(Font.font("Calibri", 20));
        imgLabel.setGraphic(imageView2);
        VBox folderBox2 = new VBox(imgLabel);
        folderBox2.getStyleClass().add("folderBox");

        ImageView imageView3 = new ImageView(new Image(getClass().getResource("/blackIconFolder/folderMusic.png").toExternalForm()));
        imageView3.setFitWidth(50);
        imageView3.setFitHeight(50);
        Label musicLabel = new Label("Music");
        musicLabel.setFont(Font.font("Calibri", 20));
        musicLabel.setGraphic(imageView3);
        VBox folderBox3 = new VBox(musicLabel);
        folderBox3.getStyleClass().add("folderBox");

        ImageView imageView4 = new ImageView(new Image(getClass().getResource("/blackIconFolder/folderVideo.png").toExternalForm()));
        imageView4.setFitWidth(50);
        imageView4.setFitHeight(50);
        Label videoLabel = new Label("Videos");
        videoLabel.setFont(Font.font("Calibri", 20));
        videoLabel.setGraphic(imageView4);
        VBox folderBox4 = new VBox(videoLabel);
        folderBox4.getStyleClass().add("folderBox");

        //Back Button in Receive files
        Label arrowLabel = new Label();
        ImageView iview = new ImageView(new Image("arrow-left.png"));
        iview.setFitWidth(28);
        iview.setFitHeight(28);
        arrowLabel.setGraphic(iview);
        arrowLabel.setPadding(new Insets(0, 0, 0, 10));

        folderBox1.setOnMouseClicked(e -> {
            receiveFolderClickFlag = true;
            System.out.println("clicked on the document");
            filesBox.getChildren().clear();
            filesBox.getChildren().add(arrowLabel);

            File fileObj = new File("./Received");
            FilenameFilter fileFilter = (directory, name) -> FileType.fromFileName(name) == FileType.DOCUMENT;
            File[] files = fileObj.listFiles(fileFilter);
            if (files != null) {
                for (File f : files) {
                    fileNameList.add(f.getName());
                }
            }

            fileBoxLoader(files, filesBox, arrowLabel, "DOCUMENT", folderBox1, folderBox2, folderBox3, folderBox4);
//            System.out.println(fileNameList);
            if (filesBox.getChildren().size() == 1) {
                Label noFileLabel = new Label("No files found");
                noFileLabel.setPrefSize(filesBox.getWidth(), filesBox.getHeight());
                filesBox.getChildren().add(noFileLabel);
                noFileLabel.setAlignment(Pos.CENTER);
            }
        });

        folderBox2.setOnMouseClicked(e -> {
            receiveFolderClickFlag = true;
            System.out.println("clicked on the image");
            filesBox.getChildren().clear();
            filesBox.getChildren().add(arrowLabel);

            File fileObj = new File("./Received");
            FilenameFilter fileFilter = (directory, name) -> FileType.fromFileName(name) == FileType.IMAGE;
            File[] files = fileObj.listFiles(fileFilter);
            if (files != null) {
                for (File f : files) {
                    fileNameList.add(f.getName());
                }
            }

            fileBoxLoader(files, filesBox, arrowLabel, "IMAGE", folderBox1, folderBox2, folderBox3, folderBox4);
//            System.out.println(fileNameList);
            if (filesBox.getChildren().size() == 1) {
                Label noFileLabel = new Label("No files found");
                noFileLabel.setPrefSize(filesBox.getWidth(), filesBox.getHeight());
                filesBox.getChildren().add(noFileLabel);
                noFileLabel.setAlignment(Pos.CENTER);
            }
        });

        folderBox3.setOnMouseClicked(e -> {
            receiveFolderClickFlag = true;
            System.out.println("clicked on the music");
            filesBox.getChildren().clear();
            filesBox.getChildren().add(arrowLabel);

            File fileObj = new File("./Received");
            FilenameFilter fileFilter = (directory, name) -> FileType.fromFileName(name) == FileType.MUSIC;
            File[] files = fileObj.listFiles(fileFilter);
            if (files != null) {
                for (File f : files) {
                    fileNameList.add(f.getName());
                }
            }

            fileBoxLoader(files, filesBox, arrowLabel, "MUSIC", folderBox1, folderBox2, folderBox3, folderBox4);
//            System.out.println(fileNameList);
            if (filesBox.getChildren().size() == 1) {
                Label noFileLabel = new Label("No files found");
                noFileLabel.setPrefSize(filesBox.getWidth(), filesBox.getHeight());
                filesBox.getChildren().add(noFileLabel);
                noFileLabel.setAlignment(Pos.CENTER);
            }
        });

        folderBox4.setOnMouseClicked(e -> {
            receiveFolderClickFlag = true;
            System.out.println("clicked on the video");
            filesBox.getChildren().clear();
            filesBox.getChildren().add(arrowLabel);

            File fileObj = new File("./Received");
            FilenameFilter fileFilter = (directory, name) -> FileType.fromFileName(name) == FileType.VIDEO;
            File[] files = fileObj.listFiles(fileFilter);
            if (files != null) {
                for (File f : files) {
                    fileNameList.add(f.getName());
                }
            }

            fileBoxLoader(files, filesBox, arrowLabel, "VIDEO", folderBox1, folderBox2, folderBox3, folderBox4);
//            System.out.println(fileNameList);
            if (filesBox.getChildren().size() == 1) {
                Label noFileLabel = new Label("No files found");
                noFileLabel.setPrefSize(filesBox.getWidth(), filesBox.getHeight());
                filesBox.getChildren().add(noFileLabel);
                noFileLabel.setAlignment(Pos.CENTER);
            }
        });

        filesBox.getChildren().addAll(folderBox1, folderBox2, folderBox3, folderBox4);

        l1.setOnMouseClicked(e -> {
            i1.getStyleClass().add("menuContainerFirst");
            i6.getStyleClass().remove("menuContainerFirst");
            i5.getStyleClass().remove("menuContainerFirst");
            i2.getStyleClass().remove("menuContainerFirst");
            i4.getStyleClass().remove("menuContainerFirst");
            vBoxMid.setVisible(true);
            vBoxMid.setManaged(true);
            settingMid.setVisible(false);
            settingMid.setManaged(false);
            importMidScrollPane.setVisible(false);
            importMidScrollPane.setManaged(false);
            filesBoxScrollPane.setManaged(false);
            filesBoxScrollPane.setVisible(false);
            System.out.println("l1: " + root.getChildren().size());
            int rootInd = root.getChildren().indexOf(vboxRight1);
            if (rootInd > 0) {
                root.getChildren().remove(rootInd);
            }

            if (root.getChildren().contains(vboxRight2)) {
                root.getChildren().remove(vboxRight2);
                vboxRight2.getChildren().clear();
            }

            if (root.getChildren().size() == 6) {
                root.getChildren().remove(5);
            }
        });

        l2.setOnMouseClicked(e -> {
            System.out.println(root.getChildren() + "\n" + root.getChildren().size());
            i2.getStyleClass().add("menuContainerFirst");
            i6.getStyleClass().remove("menuContainerFirst");
            i5.getStyleClass().remove("menuContainerFirst");
            i1.getStyleClass().remove("menuContainerFirst");
            i4.getStyleClass().remove("menuContainerFirst");
            settingMid.setVisible(true);
            settingMid.setManaged(true);
            vBoxMid.setVisible(false);
            vBoxMid.setManaged(false);
            importMidScrollPane.setVisible(false);
            importMidScrollPane.setManaged(false);
            filesBoxScrollPane.setManaged(false);
            filesBoxScrollPane.setVisible(false);
//            System.out.println("l2: " + root.getChildren());
            if (root.getChildren().size() == 6) {
                root.getChildren().remove(5);
            }
        });

        File exportChatFolder = new File("src/main/resources/exportChat");

        BorderPane chatLogHeading = new BorderPane();
        chatLogHeading.setPadding(new Insets(10));
        Label chatLogTopLabel = new Label("Chat Logs");
        chatLogTopLabel.getStyleClass().add("chatLogTopLabel");
        chatLogHeading.setCenter(chatLogTopLabel);

        if (exportChatFolder.listFiles(f -> f.getName().toLowerCase().endsWith(".json")).length > 0) {
            importMid.getChildren().clear();
            importMid.getChildren().add(chatLogHeading);
            importMid.setAlignment(Pos.TOP_CENTER);
            for (File i : exportChatFolder.listFiles(f -> f.getName().toLowerCase().endsWith(".json"))) {
                importChatFiles(i, i.getName(), root, importMid, vboxRight2, vboxRight2Label);
            }
        }

        FolderWatcherTask watcher = new FolderWatcherTask(this, Paths.get("src/main/resources/exportChat"), root, importMid, vboxRight2, vboxRight2Label);
        Thread watcherThread = new Thread(watcher);
        watcherThread.start();

        l4.setOnMouseClicked(e -> {
            i4.getStyleClass().add("menuContainerFirst");
            i6.getStyleClass().remove("menuContainerFirst");
            i5.getStyleClass().remove("menuContainerFirst");
            i1.getStyleClass().remove("menuContainerFirst");
            i2.getStyleClass().remove("menuContainerFirst");
            if (root.getChildren().size() == 6) {
                root.getChildren().remove(5);
            }
            importMidScrollPane.setVisible(true);
            importMidScrollPane.setManaged(true);
            settingMid.setVisible(false);
            settingMid.setManaged(false);
            vBoxMid.setVisible(false);
            vBoxMid.setManaged(false);
            filesBoxScrollPane.setManaged(false);
            filesBoxScrollPane.setVisible(false);

            System.out.println(root.getChildren());
        });

        l5.setOnMouseClicked(e -> {
            i5.getStyleClass().add("menuContainerFirst");
            i6.getStyleClass().remove("menuContainerFirst");
            i4.getStyleClass().remove("menuContainerFirst");
            i1.getStyleClass().remove("menuContainerFirst");
            i2.getStyleClass().remove("menuContainerFirst");

            filesBoxScrollPane.setManaged(true);
            filesBoxScrollPane.setVisible(true);
            importMidScrollPane.setVisible(false);
            importMidScrollPane.setManaged(false);
            settingMid.setVisible(false);
            settingMid.setManaged(false);
            vBoxMid.setVisible(false);
            vBoxMid.setManaged(false);
            System.out.println(root.getChildren());
            if (root.getChildren().size() == 6) {
                root.getChildren().removeLast();
            }

            if (isMediaBox) {
                root.getChildren().add(musicTempBox);
            }
        });

        l6.setOnMouseClicked(e -> {
            System.out.println("width of vboxMid: " + vBoxMid.getWidth());
            System.out.println("height of vboxMid: " + vBoxMid.getHeight());
            i6.getStyleClass().add("menuContainerFirst");
            i5.getStyleClass().remove("menuContainerFirst");
            i4.getStyleClass().remove("menuContainerFirst");
            i1.getStyleClass().remove("menuContainerFirst");
            i2.getStyleClass().remove("menuContainerFirst");

            filesBoxScrollPane.setManaged(false);
            filesBoxScrollPane.setVisible(false);
            importMidScrollPane.setVisible(false);
            importMidScrollPane.setManaged(false);
            settingMid.setVisible(false);
            settingMid.setManaged(false);
            vBoxMid.setVisible(false);
            vBoxMid.setManaged(false);
            System.out.println(root.getChildren());
            if (root.getChildren().size() == 6) {
                root.getChildren().removeLast();
            }

            root.getChildren().add(createGroupUI(midLowerBox));
        });

        i3.setOnMouseClicked(e -> {
            if (toggleSwitch.isOn()) {
                isDark = true;
                if (root.getChildren().size() == 5 && root.getChildren().contains(vboxRight2)) {
                    ScrollPane spane = (ScrollPane) vboxRight2.getChildren().getLast();
                    spane.getContent().setStyle("-fx-background-color: #283747");
                    VBox vb = (VBox) spane.getContent();
                    for (int ind = 0; ind < vb.getChildren().size(); ind++) {
                        HBox h1 = (HBox) vb.getChildren().get(ind);
                        if (h1.getChildren().getFirst() instanceof BorderPane) {
                            BorderPane bpane = (BorderPane) h1.getChildren().getFirst();
                            bpane.getCenter().getStyleClass().remove("timeStamp");
                            bpane.getCenter().getStyleClass().add("timeStampDark");
                        } else {
                            BorderPane bpane = (BorderPane) h1.getChildren().getLast();
                            bpane.getCenter().getStyleClass().remove("timeStamp");
                            bpane.getCenter().getStyleClass().add("timeStampDark");
                        }
                    }
                }

                if (isMediaBox) {
                    VBox box = (VBox) root.getChildren().getLast();
                    box.setStyle("-fx-background-color: #283747");
                    VBox mainLayout = (VBox) box.getChildren().getFirst();
                    mainLayout.getChildren().get(1).setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white");
                    ((VBox) mainLayout.getChildren().get(2)).getChildren().getLast().setStyle("-fx-text-fill: white");
                    ((HBox) mainLayout.getChildren().getLast()).getChildren().getFirst().setStyle("-fx-text-fill: white");
                }

                i3.getChildren().set(0, darkMode);
                seachbarBox.setStyle("-fx-background-color: #1b2631;");
                midLowerBox.setStyle("-fx-background-color: #1b2631;");
                root.setBackground(Background.fill(Color.web("#283747")));
                settingMid.setStyle("-fx-background-color: #1b2631;");
                importMid.setStyle("-fx-background-color: #1b2631;");
                chatLogTopLabel.setStyle("-fx-text-fill: white;");
                searchResultBox.setStyle("-fx-background-color: #1b2631;");
                vboxRight1.setStyle("-fx-background-color: #283747");
                heading.setStyle("-fx-text-fill: white");
                currentName.setStyle("-fx-text-fill: white");
                vBoxMid.getStyleClass().remove("vboxMid");
                vBoxMid.getStyleClass().add("vboxMidDark");
                darkMode(midLowerBox, settingMid, importMid, filesBox);
            } else {
                isDark = false;
                if (root.getChildren().size() == 5 && root.getChildren().contains(vboxRight2)) {
                    vBoxMid.getStyleClass().remove("vboxMid");
                    vBoxMid.getStyleClass().add("vboxMidDark");
                    ScrollPane spane = (ScrollPane) vboxRight2.getChildren().getLast();
                    spane.getContent().setStyle("-fx-background-color: #f8f9f9");
                    VBox vb = (VBox) spane.getContent();
                    for (int ind = 0; ind < vb.getChildren().size(); ind++) {
                        HBox h1 = (HBox) vb.getChildren().get(ind);
                        if (h1.getChildren().getFirst() instanceof BorderPane) {
                            BorderPane bpane = (BorderPane) h1.getChildren().getFirst();
                            bpane.getCenter().getStyleClass().remove("timeStampDark");
                            bpane.getCenter().getStyleClass().add("timeStamp");
                        } else {
                            BorderPane bpane = (BorderPane) h1.getChildren().getLast();
                            bpane.getCenter().getStyleClass().remove("timeStampDark");
                            bpane.getCenter().getStyleClass().add("timeStamp");
                        }
                    }
                }
                if (isMediaBox) {
                    VBox box = (VBox) root.getChildren().getLast();
                    box.setStyle("-fx-background-color: white");
                    VBox mainLayout = (VBox) box.getChildren().getFirst();
                    mainLayout.getChildren().get(1).setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: black");
                    ((VBox) mainLayout.getChildren().get(2)).getChildren().getLast().setStyle("-fx-text-fill: black");
                    ((HBox) mainLayout.getChildren().getLast()).getChildren().getFirst().setStyle("-fx-text-fill: black");
                }
                i3.getChildren().set(0, brightMode);
                seachbarBox.setStyle("-fx-background-color: white;");
                midLowerBox.setStyle("-fx-background-color: white;");
                settingMid.setStyle("-fx-background-color: white;");
                importMid.setStyle("-fx-background-color: white;");
                chatLogTopLabel.setStyle("-fx-text-fill: black;");
                searchResultBox.setStyle("-fx-background-color: white;");
                vboxRight1.setStyle("-fx-background-color: #f8f9f9");
                heading.setStyle("-fx-text-fill: black");
                currentName.setStyle("-fx-text-fill: black");
                vBoxMid.getStyleClass().remove("vboxMidDark");
                vBoxMid.getStyleClass().add("vboxMid");
                root.setBackground(Background.fill(Color.web("#f4f4f4")));
                originalMode(midLowerBox, settingMid, importMid, filesBox);
            }
        });

        settingMid.getChildren().addAll(accountNameBox, passwordChangeBox, photoChangeBox, deleteAccountBox);
        root.getChildren().addAll(vboxLeft, vBoxMid, settingMid, importMidScrollPane, filesBoxScrollPane);

        new Thread(() -> {
            clientServer = new thisServer(this);
            client.clientHandler(mobile.toString(), this);
        }).start();

        Scene scene = new Scene(root, appWidth, appHeight);
        scene.getStylesheets().add(getClass().getResource("/mainPageStyle.css").toExternalForm());
        return scene;
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            mobile = Long.valueOf(rdf.getUserMobile());
            fileDownload.app = this;
            commanStage = primaryStage;
            primaryStage.setScene(mainPage(commanStage));
            primaryStage.show();
            primaryStage.setTitle("First App");
            primaryStage.setOnCloseRequest(e -> {
                System.out.println("Close button (X) clicked!");
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
