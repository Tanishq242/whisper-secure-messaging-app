package org.chattingapp.mychatapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

class writeJsonFile {
    List<exportChat> chatList = new ArrayList<>();
    msgEncodeDecode msgEncodeDecode = new msgEncodeDecode();

    public int createJsonFile() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", "chatbot");
        data.put("mobile", 1111111111L);
        data.put("img_path", "/ai.png");
        data.put("public_key", null);

        List<Map<String, Object>> list = new ArrayList<>();
        list.add(data);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File("src/main/resources/myData.json"), list);
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void userInfoCreateFile(String name, Long mobile, String img_url) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", name);
        data.put("mobile", mobile);
        data.put("img_path", img_url);

        List<Map<String, Object>> list = new ArrayList<>();
        list.add(data);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File("src/main/resources/userInfo.json"), list);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateProfilePicLocation(String path) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File("src/main/resources/userInfo.json");
            JsonNode root = objectMapper.readTree(file);
            ((ObjectNode) root.get(0)).put("img_path", path);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(String name, Long num, String img_url, String pKey) {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File("src/main/resources/myData.json");
        ArrayNode arrayNode;

        try {
            // Load existing data or create new array
            if (file.exists() && file.length() != 0) {
                arrayNode = (ArrayNode) objectMapper.readTree(file);
            } else {
                arrayNode = objectMapper.createArrayNode();
            }

            // Check for duplicate
            boolean duplicateFound = false;
            for (JsonNode node : arrayNode) {
                String existingName = node.get("name").asText();
                long existingNum = node.get("mobile").asLong();

                if (existingName.equals(name) && existingNum == num) {
                    duplicateFound = true;
                    break;
                }
            }

            if (duplicateFound) {
                System.out.println("Duplicate entry found. Skipping write.");
            } else {
                // Create new JSON object
                ObjectNode jsonNode = objectMapper.createObjectNode();
                jsonNode.put("name", name);
                jsonNode.put("mobile", num);
                jsonNode.put("img_path", img_url);
                jsonNode.put("public_key", pKey);

                // Add and save
                arrayNode.insert(0, jsonNode);
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, arrayNode);

                System.out.println("Data saved: " + name + ", " + num);
            }

        } catch (IOException e) {
            System.out.println("Error writing JSON: " + e.getMessage());
        }
    }

    public void removePerson(String id) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // Read the JSON file into a JsonNode
            File file = new File("src/main/resources/myData.json");
            ArrayNode arrayNode = (ArrayNode) mapper.readTree(file);

            // Remove object with id == 2
            Iterator<JsonNode> iterator = arrayNode.iterator();
            while (iterator.hasNext()) {
                JsonNode node = iterator.next();
                if (node.has("mobile") && node.get("mobile").asText().equals(id)) {
                    iterator.remove();
                }
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, arrayNode);
            System.out.println("Object removed and file updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void exportJson(VBox chatArea) {
        String[] parts;
        String encryptMsg = null;
        for (int i = 0; i < chatArea.getChildren().size(); i++) {
            HBox hBox = (HBox) chatArea.getChildren().get(i);
            Node node = hBox.getChildren().getFirst();
            if (node instanceof StackPane) {
                String labelId = ((StackPane) node).getChildren().getFirst().getId();
                parts = labelId.split("\\|");
                try {
                    encryptMsg = msgEncodeDecode.symmetricEncrypt(parts[3]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                chatList.add(new exportChat(parts[0], parts[1], parts[2], encryptMsg, parts[4]));
            } else {
                node = hBox.getChildren().getLast();
                StackPane spNode = (StackPane) node;
                Node n = spNode.getChildren().getFirst();
                if (n instanceof Label) {
                    String labelId = spNode.getChildren().getFirst().getId();
                    parts = labelId.split("\\|");
                } else {
                    VBox box = (VBox) n;
                    String labelId = box.getChildren().getFirst().getId();
                    parts = labelId.split("\\|");
                }
                try {
                    encryptMsg = msgEncodeDecode.symmetricEncrypt(parts[3]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                chatList.add(new exportChat(parts[0], parts[1], parts[2], encryptMsg, parts[4]));
            }
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_hh-mm-ss");
        String fDateTime = now.format(formatter);
        try (FileWriter writer = new FileWriter("src/main/resources/exportChat/"+fDateTime+".json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(chatList, writer);
            System.out.println("Chat exported successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeReceivedFilesJson(String uname, Long mobile, String fileId, String fileName, double fileSize, String receivedTime, String fileKey) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // Read or create array
            File f = new File("src/main/resources/receivedFileJson/output.json");
            ArrayNode arr = f.exists() ? (ArrayNode) mapper.readTree(f) : mapper.createArrayNode();

            // Find mobile or create new
            ObjectNode entry = null;
            for (int i = 0; i < arr.size(); i++) {
                if (arr.get(i).has("mobile") && arr.get(i).get("mobile").asLong() == mobile) {
                    entry = (ObjectNode) arr.get(i);
                    break;
                }
            }

            if (entry == null) {
                entry = mapper.createObjectNode();
                entry.put("mobile", mobile);
                entry.put("name", uname);
                entry.set("field", mapper.createArrayNode());
                arr.add(entry);
            }

            // Get or create field array
            ArrayNode fieldArray;
            if (entry.has("field") && entry.get("field").isArray()) {
                fieldArray = (ArrayNode) entry.get("field");
            } else {
                fieldArray = mapper.createArrayNode();
                entry.set("field", fieldArray);
            }

            // Add field
            ObjectNode field = mapper.createObjectNode();
            field.put("fileId", fileId);
            field.put("fileName", fileName.trim());
            field.put("size", fileSize);
            field.put("receivedTime", receivedTime);
            field.put("fileKey", fileKey);
            field.put("split", false);
            field.put("downloaded", false);
            fieldArray.add(field);

            // Save
            mapper.writerWithDefaultPrettyPrinter().writeValue(f, arr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateSplitField(String mobile, String fileName, Boolean value) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("src/main/resources/receivedFileJson/output.json");
            ArrayNode arrayNode = (ArrayNode) mapper.readTree(file);

            if (file.exists()) {
                for (JsonNode objNode : arrayNode) {
                    if (objNode.has("mobile") && objNode.get("mobile").asText().equals(mobile)) {
                        JsonNode fields = objNode.get("field");
                        if (fields.isArray()) {
                            for (JsonNode fieldNode : fields) {
                                if (fieldNode.get("fileId").asText().equals(fileName)) {
                                    ((ObjectNode) fieldNode).put("split", value);
                                }
                            }
                        }
                        break;
                    }
                }
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, arrayNode);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateDownloadField(String mobile, String fileName) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("src/main/resources/receivedFileJson/output.json");
            ArrayNode arrayNode = (ArrayNode) mapper.readTree(file);

            if (file.exists()) {
                for (JsonNode objNode : arrayNode) {
                    if (objNode.has("mobile") && objNode.get("mobile").asText().equals(mobile)) {
                        JsonNode fields = objNode.get("field");
                        if (fields.isArray()) {
                            for (JsonNode fieldNode : fields) {
                                if (fieldNode.get("fileId") != null &&
                                        fieldNode.get("fileId").asText().equals(fileName)) {
                                    // ✅ Cast fieldNode to ObjectNode, not fieldNode.get("downloaded")
                                    ((ObjectNode) fieldNode).put("downloaded", true);
                                }
                            }
                        }
                        break;
                    }
                }

                // ✅ Write changes back to file
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, arrayNode);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
