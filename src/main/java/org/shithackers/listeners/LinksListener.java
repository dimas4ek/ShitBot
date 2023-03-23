package org.shithackers.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinksListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String messageContent = event.getMessage().getContentRaw();
        Pattern pattern = Pattern.compile("(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\))))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\))))*\\b)");
        Matcher matcher = pattern.matcher(messageContent);

        if (matcher.find()) {
            OkHttpClient client = new OkHttpClient();

            String url = "https://www.virustotal.com/api/v3/urls";
            String apiKey = "***";

            RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("url", matcher.group())
                .build();

            Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .header("x-apikey", apiKey)
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);
                    String dataId = json.getJSONObject("data").getString("id");
                    String dataUrl = "https://www.virustotal.com/api/v3/analyses/" + dataId;

                    Request request2 = new Request.Builder()
                        .url(dataUrl)
                        .header("x-apikey", apiKey)
                        .build();

                    try (Response response2 = client.newCall(request2).execute()) {
                        if (response2.isSuccessful()) {
                            String responseBody2 = response2.body().string();
                            JSONObject json2 = new JSONObject(responseBody2);
                            int sus = json2.getJSONObject("data").getJSONObject("attributes").getJSONObject("stats").getInt("suspicious");
                            int mal = json2.getJSONObject("data").getJSONObject("attributes").getJSONObject("stats").getInt("malicious");

                            if (mal > 2) {
                                event.getMessage().reply("this site is malicious").queue();
                            } else if (sus > 0) {
                                event.getMessage().reply("this site is suspicious").queue();
                            }
                        } else {
                            System.out.println("Error: " + response2.code() + " " + response2.message());
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
