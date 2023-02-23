package org.shithackers.commands.info;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserInfoCommand extends ListenerAdapter
{
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getFullCommandName().equals("userinfo")) {
            User user = event.getOption("user").getAsUser();
            List<String> urls = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader("shitImagesURLs.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    urls.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            event.replyEmbeds(
                new EmbedBuilder()
                    .setTitle("User Info")
                    .addField("User name", user.getAsMention(), true)
                    .addField("User ID", user.getId(), true)
                    .addField("User created", user.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME), false)
                    .setImage(urls.get(new Random().nextInt(urls.size())))
                    .setThumbnail(user.getAvatarUrl())
                    .build()
            ).queue();
        }
    }


}