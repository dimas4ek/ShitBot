package org.shithackers.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager {

    private static PlayerManager INSTANCE;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager playerManager;

    public PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(this.playerManager);
        AudioSourceManagers.registerLocalSource(this.playerManager);
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager musicManager = new GuildMusicManager(this.playerManager);
            guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
            return musicManager;
        });
    }

    public void loadAndPlay(TextChannel channel, SlashCommandInteractionEvent event, String trackUrl) {
        final GuildMusicManager musicManager = this.getMusicManager(channel.getGuild());
        event.deferReply().queue();
        this.playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                musicManager.scheduler.queue(audioTrack);

                EmbedBuilder builder = getEmbedBuilder(event, audioTrack);

                event.getHook().sendMessageEmbeds(builder.build()).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                final List<AudioTrack> tracks = audioPlaylist.getTracks();
                if (!tracks.isEmpty()) {
                    musicManager.scheduler.queue(tracks.get(0));
                    EmbedBuilder builder = getEmbedBuilder(event, tracks.get(0));

                    event.getHook().sendMessageEmbeds(builder.build()).queue();
                }
            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException e) {

            }
        });
    }

    public void skipTrack(TextChannel asTextChannel) {
        final GuildMusicManager musicManager = this.getMusicManager(asTextChannel.getGuild());
        musicManager.scheduler.nextTrack();
    }

    public void stopTrack(TextChannel asTextChannel) {
        final GuildMusicManager musicManager = this.getMusicManager(asTextChannel.getGuild());
        musicManager.scheduler.player.stopTrack();
    }

    public static PlayerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

    @NotNull
    private static EmbedBuilder getEmbedBuilder(SlashCommandInteractionEvent event, AudioTrack track) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Music");
        builder.addField("Added by", event.getUser().getAsMention(), false);
        builder.addField("Music", track.getInfo().title, false);
        builder.addField("Author", track.getInfo().author, false);
        builder.addField("Duration", track.getInfo().length + "ms", false);
        builder.setFooter("Requested by " + event.getUser().getAsTag(), event.getUser().getAvatarUrl());
        builder.setColor(0x00ff00);
        return builder;
    }
}
