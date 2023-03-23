package org.shithackers.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class GuildMusicManager {
    private final AudioPlayer player;
    final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;

    public GuildMusicManager(AudioPlayerManager playerManager) {
        this.player = playerManager.createPlayer();
        this.scheduler = new TrackScheduler(this.player);
        this.player.addListener(this.scheduler);
        this.sendHandler = new AudioPlayerSendHandler(player);
    }

    public AudioPlayerSendHandler getSendHandler() {
        return this.sendHandler;
    }
}
