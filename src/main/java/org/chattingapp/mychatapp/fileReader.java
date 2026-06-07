package org.chattingapp.mychatapp;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class fileReader {
    public int createFile(String name, String mobile) {
        try (FileWriter fileWriter = new FileWriter("src/main/resources/accountInfo.txt");) {
            fileWriter.write(name.toLowerCase()+":"+mobile);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String readName() {
        try {
            File file = new File("src/main/resources/accountInfo.txt");
            Scanner sc = new Scanner(file);
            String txt = sc.nextLine();
            String uname = txt.substring(0, txt.indexOf(":"));
            String modified = uname.toUpperCase().charAt(0) + uname.toLowerCase().substring(1);
            return modified;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "none";
    }

    public String readMobile() {
        try {
            File file = new File("src/main/resources/accountInfo.txt");
            Scanner sc = new Scanner(file);
            String txt = sc.nextLine();
            String mobile = txt.substring(txt.indexOf(":")+1);
            return mobile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "none";
    }

    public void updateName(String name) {
        try {
            String mobile = readMobile();
            FileWriter fileWriter = new FileWriter("src/main/resources/accountInfo.txt");
            fileWriter.write(name.toLowerCase().trim()+":"+mobile);
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int deleteFile(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
                return 1;
            } else {
                return 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String searchFileName() {
        File folder = new File("src/main/resources/profilePicture");
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Folder not found or is not a directory.");
            return null;
        }
        File[] files = folder.listFiles();
        if (files == null) return null;

        for (File file : files) {
            if (file.isFile()) {
                String name = file.getName();
                return name;
            }
        }

        return null;
    }

    public int renameFile(String oldOne, String newOne) {
        File oldFile = new File("src/main/resources/profilePicture/"+oldOne);
        File newFile = new File("src/main/resources/profilePicture/"+newOne);

        boolean success = oldFile.renameTo(newFile);

        if (success) {
            return 1;
        } else {
            return 0;
        }
    }

    public void exportFile (VBox chatArea) {
        String[] parts;
        String formattedText;
        try {
            FileWriter writer = new FileWriter("src/main/resources/exportChat/chat_backup.txt");

            for (int i = 0; i < chatArea.getChildren().size(); i++) {
                HBox hBox = (HBox) chatArea.getChildren().get(i);
                Node node = hBox.getChildren().getFirst();
                if (node instanceof StackPane) {
                    String labelId = ((StackPane) node).getChildren().getFirst().getId();
                    parts = labelId.split("\\|");
                    formattedText = "["+parts[0]+"] "+parts[1]+": "+parts[3]+"\n";
                    writer.write(formattedText);
                } else {
                    node = hBox.getChildren().getLast();
                    String labelId = ((StackPane) node).getChildren().getFirst().getId();
                    parts = labelId.split("\\|");
                    formattedText = "["+parts[0]+"] "+parts[1]+": "+parts[3]+"\n";
                    writer.write(formattedText);
                }
            }

            writer.close();
            System.out.println("Chat saved to chat_backup.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
