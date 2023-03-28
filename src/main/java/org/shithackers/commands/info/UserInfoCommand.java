package org.shithackers.commands.info;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class UserInfoCommand extends ListenerAdapter
{
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getFullCommandName().equals("user")) {
            List<String> urls = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader("shitImagesURLs.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    urls.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Member member = Objects.requireNonNull(event.getGuild()).getMember(Objects.requireNonNull(event.getOption("user")).getAsUser());

            assert member != null;
            event.replyEmbeds(
                new EmbedBuilder()
                    .setTitle("User Info")
                    .addField("User name", member.getAsMention(), false)
                    .addField("User created", member.getTimeCreated().format(DateTimeFormatter.ofPattern("MMM. dd, yyyy")), true)
                    .addField("User joined", member.getTimeJoined().format(DateTimeFormatter.ofPattern("MMM. dd, yyyy")), true)
                    .addField("Roles", member.isOwner() ? "OWNER" : member.hasPermission(Permission.ADMINISTRATOR) ? "ADMIN" : member.hasPermission(Permission.MODERATE_MEMBERS) ? "MODERATOR" : "None", false)
                    .setImage(urls.get(new Random().nextInt(urls.size())))
                    .setThumbnail(member.getEffectiveAvatarUrl())
                    .build()
            ).queue();

        }
    }
}