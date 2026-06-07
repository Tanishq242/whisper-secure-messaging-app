package org.chattingapp.mychatapp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

class translatorCode {
    private static String key = System.getenv("AZURE_TRANSLATOR_KEY");;

    // location, also known as region.
    // required if you're using a multi-service or regional (not global) resource. It can be found in the Azure portal on the Keys and Endpoint page.
    private static String location = "centralindia";


    // Instantiates the OkHttpClient.
    OkHttpClient client = new OkHttpClient();

    // This function performs a POST request.
    public String Post(String txt, String translateLang) {
        try {
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "[{\"Text\": \"" + txt + "\"}]");
            Request request = new Request.Builder()
                    .url("https://api.cognitive.microsofttranslator.com/translate?api-version=3.0&to=" + translateLang)
                    .post(body)
                    .addHeader("Ocp-Apim-Subscription-Key", key)
                    // location required if you're using a multi-service or regional (not global) resource.
                    .addHeader("Ocp-Apim-Subscription-Region", location)
                    .addHeader("Content-type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    // This function prettifies the json response.
    public String prettify(String json_text) {
        JsonParser parser = new JsonParser();
        JsonElement json = parser.parse(json_text);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
    }

    public String getTranslation(String response) {
        JSONArray rootArray = new JSONArray(response);
        JSONObject firstObject = rootArray.getJSONObject(0);
        JSONArray translations = firstObject.getJSONArray("translations");
        JSONObject translation = translations.getJSONObject(0);

        String translatedText = translation.getString("text");
        return translatedText;
    }
}