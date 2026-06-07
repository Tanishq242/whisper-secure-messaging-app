package org.chattingapp.mychatapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.json.JSONObject;

import java.util.Map;

public class GenerateOTP {
    private static String apiKey = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJDLTc4NDZBOEVDOEU5MzQzNCIsImlhdCI6MTc2MzY1Mzg3MCwiZXhwIjoxOTIxMzMzODcwfQ.pethcE_unjEXUGhLWiq91mRhUw2rbtu1nbngKoH7aXWifrwqVczLIpB_2wARc9H15LUqn4Pa-RG7UxmNndCI8A";

    public static int sendOTP(long mobile) {
        try {
            OkHttpClient client = new OkHttpClient()
                    .newBuilder()
                    .build();

            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, "");
            Request request = new Request.Builder()
                    .url("https://cpaas.messagecentral.com/verification/v3/send?countryCode=91&customerId=C-7846A8EC8E93434&flowType=SMS&mobileNumber="+mobile)
                    .method("POST", body)
                    .addHeader("authToken", apiKey)
                    .build();

            Response response = client.newCall(request).execute();

            return 1;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String verifyOTP (long mobile, int otp, int vid) {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, "");
            Request request = new Request.Builder()
                    .url("https://cpaas.messagecentral.com/verification/v3/validateOtp?countryCode=91&mobileNumber="+mobile+"&verificationId="+vid+"&customerId=C-7846A8EC8E93434&code="+otp)
                    .method("GET", null)
                    .addHeader("authToken", apiKey)
                    .build();
            Response response = client.newCall(request).execute();
            JSONObject json = new JSONObject(response);
            return json.toString(4);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getResponse(String jsonResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            int responseCode = root.get("responseCode").asInt();
            int verificationId = root.get("data").get("verificationId").asInt();
            return responseCode+"_"+verificationId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(sendOTP(9953744795L));
    }
}
