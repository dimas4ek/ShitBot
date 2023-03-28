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
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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
                        TextChannel channel = Objects.requireNonNull(event.getOption("channel")).getAsChannel().asTextChannel();

                        event.reply("Started").setEphemeral(true).queue();
                        channel.sendMessage(replies.get(new Random().nextInt(replies.size()))).queue();

                        TimeUnit.HOURS.sleep(3);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        if (event.getFullCommandName().equals("quote stop")) {
            event.reply("Stopped").queue();
            running = false;
        }
    }
}
