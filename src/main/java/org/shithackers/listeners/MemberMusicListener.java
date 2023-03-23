package org.shithackers.listeners;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.shithackers.lavaplayer.PlayerManager;

public class MemberMusicListener extends ListenerAdapter {
    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        if(event.getMember().getUser().isBot()) return;
        VoiceChannel channel = event.getChannelJoined().asVoiceChannel();
        if(channel.getMembers().size() == 1) {
            channel.getGuild().kickVoiceMember(event.getGuild().getMemberById("1073967546665013328")).queue();
        }

    }
}
