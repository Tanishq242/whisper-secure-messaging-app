package org.chattingapp.mychatapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Random;

public class GroupJsonGenerator {
    static String groupFilePath = "./GroupJson/groupsData.json";

    /**
     * Creates or appends group data to a JSON file
     *
     * @param groupName Name of the group
     * @param password  Password of Account
     * @param members   Map of mobile number to name (mobile -> name)
     * @return 1 if successful, 0 if failed
     */
    public static long createGroup(String groupName, String adminName, String password, Map<Long, String> members) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            File file = new File(groupFilePath);

            // Create parent directories if they don't exist
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            List<Map<String, Object>> groups = new ArrayList<>();
            Set<Long> existingIds = new HashSet<>();

            // If file exists, read existing groups
            if (file.exists()) {
                try {
                    groups = mapper.readValue(file, new TypeReference<List<Map<String, Object>>>() {
                    });

                    // Collect existing group IDs
                    for (Map<String, Object> g : groups) {
                        if (g.containsKey("groupId")) {
                            existingIds.add(((Number) g.get("groupId")).longValue());
                        }
                    }
                } catch (Exception e) {
                    groups = new ArrayList<>();
                }
            }

            // Generate unique group ID
            Random random = new Random();
            long groupId;
            do {
                groupId = Math.abs(random.nextLong() % 10000000000L);
                if (groupId < 1000000000L) {
                    groupId += 1000000000L;
                }
            } while (existingIds.contains(groupId));

            // Generate random keys
            int status = keyGen.createGroupKey(password);
            if (status == 1) {
                String publicKey = msgEncodeDecode.groupPublicKey(password);

                // Create member list
                List<Map<String, Object>> memberList = new ArrayList<>();
                for (Map.Entry<Long, String> entry : members.entrySet()) {
                    Map<String, Object> member = new HashMap<>();
                    member.put("memberId", entry.getKey());
                    member.put("memberName", entry.getValue());
                    memberList.add(member);
                }

                // Create new group
                Map<String, Object> group = new HashMap<>();
                group.put("groupId", groupId);
                group.put("groupName", groupName);
                group.put("groupAdmin", adminName);
                group.put("groupAdmin_2", null);
                group.put("membersCount", members.size());
                group.put("groupPublicKey", publicKey);
                group.put("memberDetails", memberList);

                // Add to list and write to file
                groups.add(group);
                mapper.writeValue(file, groups);

                return groupId; // Success
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Failure
        }
        return 0;
    }

    public static void updateGroupName(String groupName, String groupId) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("./GroupJson/groupsData.json");
            if (!file.exists() && file.length() == 0) return;
            ArrayNode arrayNode = (ArrayNode) mapper.readTree(file);

//            System.out.println(groupName);
//            System.out.println(groupId);
            for (JsonNode node : arrayNode) {
                if (node.get("groupId").asText().equals(groupId)) {
                    ((ObjectNode) node).put("groupName", groupName);
                    break;
                }
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, arrayNode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addMember(String groupId, String memberName, long memberId) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("./GroupJson/groupsData.json");

            if (!file.exists() || file.length() == 0) return;

            // Read JSON array from file
            ArrayNode rootArray = (ArrayNode) mapper.readTree(file);

            // Loop groups
            for (JsonNode groupNode : rootArray) {

                if (groupNode.get("groupId").asText().equals(groupId)) {

                    // Get existing memberDetails array as ArrayNode
                    ArrayNode memberArray = (ArrayNode) groupNode.get("memberDetails");

                    // Create new member object
                    ObjectNode newMember = mapper.createObjectNode();
                    newMember.put("memberName", memberName);
                    newMember.put("memberId", memberId);

                    // Add the new member
                    memberArray.add(newMember);

                    // Update membersCount
                    ((ObjectNode) groupNode).put("membersCount", memberArray.size());

                    break;
                }
            }

            // Save updated JSON back to file
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, rootArray);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeMember(String groupId, String memberId) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("./GroupJson/groupsData.json");

            if (!file.exists() || file.length() == 0) return;

            // Read JSON array from file
            ArrayNode rootArray = (ArrayNode) mapper.readTree(file);

            for (JsonNode groupNode : rootArray) {

                if (groupNode.get("groupId").asText().equals(groupId)) {

                    // Get existing memberDetails array as ArrayNode
                    ArrayNode memberArray = (ArrayNode) groupNode.get("memberDetails");
                    // Iterate backwards to safely remove items
                    for (int i = 0; i < memberArray.size(); i++) {

                        JsonNode member = memberArray.get(i);

                        if (member.get("memberId").asText().equals(memberId)) {
                            memberArray.remove(i);   // remove safely by index
                            break;                   // stop after removing
                        }
                    }

                    // Update membersCount
                    ((ObjectNode) groupNode).put("membersCount", memberArray.size());
                    break;
                }
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, rootArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addAdmin(String groupId, String adminName, String adminId) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("./GroupJson/groupsData.json");

            if (!file.exists() || file.length() == 0) return;

            // Read JSON array
            ArrayNode rootArray = (ArrayNode) mapper.readTree(file);
            ObjectNode targetGroup = null;
            ArrayNode memberDetails = null;

            // Find group
            for (JsonNode groupNode : rootArray) {
                if (groupNode.path("groupId").asText().equals(groupId)) {
                    targetGroup = (ObjectNode) groupNode;
                    memberDetails = (ArrayNode) groupNode.path("memberDetails");
                    break;
                }
            }

            if (targetGroup == null || memberDetails == null) {
                return; // group not found
            }

            // Set admin_2
            targetGroup.put("groupAdmin_2", adminName);

            // Remove that user from memberDetails
            for (int i = 0; i < memberDetails.size(); i++) {
                JsonNode item = memberDetails.get(i);

                boolean isTarget =
                        adminName.equals(item.path("memberName").asText()) &&
                                adminId.equals(item.path("memberId").asText());

                if (isTarget) {
                    memberDetails.remove(i);
                    break;
                }
            }

            // Write file
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, rootArray);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteGroup(String groupId) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("./GroupJson/groupsData.json");

            if (!file.exists() || file.length() == 0) return;

            // Read JSON array
            ArrayNode rootArray = (ArrayNode) mapper.readTree(file);
            for (int i = 0; i < rootArray.size(); i++) {
                if (rootArray.get(i).get("groupId").asText().equals(groupId)) rootArray.remove(i);
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, rootArray);

            if (rootArray.isEmpty()) file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}