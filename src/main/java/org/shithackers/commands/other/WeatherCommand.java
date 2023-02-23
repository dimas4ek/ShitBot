package org.shithackers.commands.other;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class WeatherCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getFullCommandName().equals("weather")) {
            String city = event.getOption("city").getAsString();
            String weatherApiKey = "a72f274b6bd6f873d04b7e7e28c40fb5";
            String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + weatherApiKey;

            try {
                URLConnection connection = new URL(url).openConnection();
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                JSONObject json = new JSONObject(response.toString());
                JSONObject main = json.getJSONObject("main");
                JSONObject wind = json.getJSONObject("wind");
                double temp = main.getDouble("temp");
                double feels = main.getDouble("feels_like");
                double humidity = main.getDouble("humidity");
                int windSpeed = wind.getInt("speed");

                event.replyEmbeds(
                        new EmbedBuilder()
                            .setTitle(city)
                            .addField("Temperature", Math.round(temp - 273.15) + " °C", true)
                            .addField("Feels like", Math.round(feels - 273.15) + " °C", true)
                            .addField("Humidity", humidity + "%", false)
                            .addField("Wind", windSpeed + " m/s", false)
                            .build()
                    ).queue();
            } catch (IOException e) {
                e.printStackTrace();
                event.reply("Sorry, I was unable to get the weather information for " + city + ".").queue();
            }
        }
    }
}
