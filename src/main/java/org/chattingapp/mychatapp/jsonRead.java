package org.chattingapp.mychatapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.File;

class readJsonFile {
    public String getId(int index) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File("src/main/resources/myData.json");
            ArrayNode arrayNode = (ArrayNode) objectMapper.readTree(file);
            return arrayNode.get(index).get("mobile").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getPublicKey(int index) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File("src/main/resources/myData.json");
            ArrayNode arrayNode = (ArrayNode) objectMapper.readTree(file);
            return arrayNode.get(index).get("public_key").asText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getUserName() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File("src/main/resources/userInfo.json");
            ArrayNode arrayNode = (ArrayNode) objectMapper.readTree(file);
            return arrayNode.get(0).get("name").asText();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getUserMobile() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File("src/main/resources/userInfo.json");
            ArrayNode arrayNode = (ArrayNode) objectMapper.readTree(file);
            return arrayNode.get(0).get("mobile").asText();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayNode read() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File("src/main/resources/myData.json");

            if (!file.exists() || file.length() == 0) {
                return null;
            }

            ArrayNode arrayNode = (ArrayNode) objectMapper.readTree(file);
            return arrayNode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean searchJson(String keyword) {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File("src/main/resources/myData.json");

        try {
            if (!file.exists() || file.length() == 0) {
                System.out.println("No data found.");
                return false;
            }

            ArrayNode arrayNode = (ArrayNode) objectMapper.readTree(file);
            boolean found = false;

            for (JsonNode node : arrayNode) {
                String name = node.get("name").asText();
                String mobile = node.get("mobile").asText();

                if (name.equalsIgnoreCase(keyword) || mobile.equals(keyword)) {
                    System.out.println("Match found:");
                    System.out.println("Name: " + name + ", Mobile: " + mobile);
                    found = true;
                    return true;
                }
            }

            if (!found) {
                System.out.println("No matching entry found for: " + keyword);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error reading JSON: " + e.getMessage());
            return false;
        }
        return false;
    }

    public static JsonNode searchGroup(String groupId) {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File("F:\\Programs\\My Projects\\myChatApp\\GroupJson\\groupsData.json");

        try {
            if (!file.exists() || file.length() == 0) {
                System.out.println("No data found.");
                return null;
            }

            ArrayNode arrayNode = (ArrayNode) objectMapper.readTree(file);
            boolean found = false;

            for (JsonNode node : arrayNode) {
                String id = node.get("groupId").asText();

                if (id.equals(groupId)) {
                    return node;
                }
            }

            if (!found) {
                System.out.println("No matching entry found for: " + groupId);
                return null;
            }
        } catch (Exception e) {
            System.out.println("Error reading JSON: " + e.getMessage());
            return null;
        }
        return null;
    }

    public ArrayNode getSenderReceiver(File file) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            if (!file.exists() || file.length() == 0) {
                System.out.println("No data found.");
                return null;
            }

            ArrayNode arrayNode = (ArrayNode) objectMapper.readTree(file);
            return arrayNode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int jsonLength() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File("src/main/resources/myData.json");
            ArrayNode arrayNode = (ArrayNode) objectMapper.readTree(file);
            return arrayNode.size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String searchFileName(Long id, String fileHash) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("src/main/resources/receivedFileJson/output.json");
            ArrayNode arrayNode = (ArrayNode) mapper.readTree(file);

            for (JsonNode objNode : arrayNode) {
                if (objNode.has("mobile") && objNode.get("mobile").asLong() == id) {
                    System.out.println("Found mobile: " + id);

                    // Iterate over the 'field' array
                    JsonNode fields = objNode.get("field");
                    if (fields.isArray()) {
                        for (JsonNode fieldNode : fields) {
                            if (fieldNode.has("fileId") && fieldNode.get("fileId").asText().equals(fileHash)) {
                                String fileName = fieldNode.get("fileName").asText();
                                return fileName.replace("encrypted_", "");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public double getFileSize(String mobile, String fileName) {
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
                                if (fieldNode.get("fileName").asText().equals(fileName)) {
                                    return fieldNode.get("size").asDouble();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return 1;
    }

    public String getFileId(String mobile, String fileName) {
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
                                if (fieldNode.get("fileName").asText().equals(fileName)) {
                                    return fieldNode.get("fileId").asText();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String getFileKey(String mobile, String fileName) {
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
                                    return fieldNode.get("fileKey").asText();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public ArrayNode getArrayNode() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("src/main/resources/myData.json");
            ArrayNode arrayNode = (ArrayNode) mapper.readTree(file);
            if (file.exists()) return arrayNode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayNode getGroupData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("./GroupJson/groupsData.json");
            if (file.exists() && file.length() != 0) {
                ArrayNode arrayNode = (ArrayNode) mapper.readTree(file);
                return arrayNode;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}