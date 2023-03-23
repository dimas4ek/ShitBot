package org.shithackers.commands.other;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PrintQuoteCommand extends ListenerAdapter {

    private boolean running = false;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        List<String> replies = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("shitcitatnik.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                replies.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (event.getFullCommandName().equalsIgnoreCase("quote random")) {
            event.reply(replies.get(new Random().nextInt(replies.size()))).queue();
        }

        if (event.getFullCommandName().equals("quote start")) {
            running = true;
            new Thread(() -> {
                while (running) {
                    try {
                        Guild guild = event.getGuild();
                        String channelName = event.getOption("channel").getAsChannel().getName();
                        TextChannel textChannel = guild.getTextChannelsByName(channelName, true).get(0);

                        event.reply("Started").queue();
                        textChannel.sendMessage(replies.get(new Random().nextInt(replies.size()))).queue();
                        Thread.sleep(1000 * 60 * 60 * 3);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else if (event.getFullCommandName().equalsIgnoreCase("quote stop")) {
            event.reply("Stopped").queue();
            running = false;
        }
    }
}
