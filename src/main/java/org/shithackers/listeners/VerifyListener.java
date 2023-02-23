package org.shithackers.listeners;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

public class VerifyListener extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        TextChannel verificationChannel = guild.getTextChannelById("975383502495297586");

        guild.modifyMemberRoles(member, guild.getRolesByName("Unverified", true)).queue();
        new Thread(() -> {
            for (TextChannel channel : guild.getTextChannels()) {
                if (!channel.getId().equals(verificationChannel.getId())) {
                    channel.getManager().putMemberPermissionOverride(member.getIdLong(), 0, Permission.VIEW_CHANNEL.getRawValue()).queue();
                }
            }
        }).start();
        verificationChannel.sendMessage("Welcome to the server, " + member.getAsMention() + "! Please type `verify` to verify yourself.").queue();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.getChannel().getId().equals("975383502495297586")) {
            if (event.getMessage().getContentRaw().equals("verify")) {
                Guild guild = event.getGuild();
                Member member = event.getMember();
                TextChannel verificationChannel = guild.getTextChannelById("975383502495297586");

                List<TextChannel> channels = guild.getTextChannels();
                for (TextChannel channel : channels) {
                    if (!channel.getId().equals(verificationChannel.getId())) {
                        channel.getMemberPermissionOverrides().forEach(override -> {
                            if (override.getMember().getId().equals(member.getId())) {
                                override.getManager().grant(Permission.VIEW_CHANNEL).queue();
                            }
                        });
                    }
                }
            }
        }
    }
}
