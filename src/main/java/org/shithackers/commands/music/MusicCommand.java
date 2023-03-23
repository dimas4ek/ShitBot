package org.shithackers.commands.music;

import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.managers.AudioManager;
import org.shithackers.lavaplayer.PlayerManager;

import java.net.URI;
import java.net.URISyntaxException;

public class MusicCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        PlayerManager manager = new PlayerManager();

        if (!event.isFromGuild()) return;
        if (event.getUser().isBot()) return;

        if (event.getName().equals("play")) {
            if (!event.getMember().getVoiceState().inAudioChannel()) {
                event.reply("You need to be in a voice channel to play music").setEphemeral(true).queue();
                return;
            }

            if (!event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
                final AudioManager audioManager = event.getGuild().getAudioManager();
                final VoiceChannel voiceChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();

                audioManager.openAudioConnection(voiceChannel);
            }

            OptionMapping mapping = event.getOption("link");
            String link;
            if (mapping != null) {
                link = mapping.getAsString();
                if (!link.startsWith("https://www.youtube.com/watch?v=")) {
                    event.reply("You need to specify a valid youtube link").setEphemeral(true).queue();
                } else {
                    PlayerManager.getInstance().loadAndPlay(event.getGuildChannel().asTextChannel(), event, link);
                }
                return;
            }
            else {
                event.reply("You need to specify a valid youtube link").setEphemeral(true).queue();
            }
        }

        if (event.getFullCommandName().equals("skip")) {
            event.reply("Skipped track").queue();
            PlayerManager.getInstance().skipTrack(event.getGuildChannel().asTextChannel());
        }

        if (event.getFullCommandName().equals("stop")) {
            event.reply("Stopped track").queue();
            PlayerManager.getInstance().stopTrack(event.getGuildChannel().asTextChannel());
        }
    }
}
