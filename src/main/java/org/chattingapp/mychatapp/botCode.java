package org.chattingapp.mychatapp;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import javafx.application.Platform;

public class botCode {
    public void sendRequest(String msg, mainCode app) {
        // The client gets the API key from the environment variable `GOOGLE_API_KEY`.
        System.out.println("This is the question: "+msg);
        String key = "AIzaSyAaM28CysGgRL5CWbwDsvKMr7OmqckvuRY";
        Client client = Client.builder().apiKey(key).build();

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-2.0-flash",
                        msg,
                        null);

        String rawResponse = response.text().replace("*", ""); // assuming it's a method
//        String formatted = rawResponse.trim().replaceAll("\\s+", " ");
        System.out.println(rawResponse.trim());
        Platform.runLater(() -> app.msgRoute("1111111111:Response:"+rawResponse.trim(), "Not Encrypted", false));
    }
}
