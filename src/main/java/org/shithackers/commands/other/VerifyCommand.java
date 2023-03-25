package org.shithackers.commands.other;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class VerifyCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getFullCommandName().equals("delete verify channel")) {

        }
        if (event.getFullCommandName().equals("channel create verify")) {
            List<TextChannel> verifyChannel = Objects.requireNonNull(event.getGuild()).getTextChannelsByName("verify", true);
            if (!verifyChannel.isEmpty()) {
                verifyChannel.forEach(channel -> channel.delete().complete());
            }

            List<Role> unverifiedRole = event.getGuild().getRolesByName("Unverified", true);
            if (!unverifiedRole.isEmpty()) {
                unverifiedRole.forEach(role -> role.delete().queue());
            }

            event.getGuild().createRole().setName("Unverified").queue((role) -> {
                role.getManager().setColor(Color.BLACK).setPermissions(Permission.EMPTY_PERMISSIONS).queue();

                event.getGuild().getMembers().forEach(member -> {
                    if (!member.getUser().isBot() && !member.hasPermission(Permission.ADMINISTRATOR, Permission.MODERATE_MEMBERS))
                        event.getGuild().modifyMemberRoles(member, role).queue();
                });

                event.getGuild()
                    .createTextChannel("verify")
                    .setTopic("Type verify to verify yourself")
                    .addPermissionOverride(Objects.requireNonNull(
                            event.getInteraction().getGuild()).getPublicRole(),
                        null,
                        EnumSet.of(Permission.VIEW_CHANNEL)
                    )
                    .addPermissionOverride(Objects.requireNonNull(
                            event.getInteraction().getGuild().getRoleById(role.getId())),
                        EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND),
                        null
                    )
                    .queue();
            });

            List<Role> verifiedRole = event.getGuild().getRolesByName("Verified", true);
            if (!verifiedRole.isEmpty()) {
                verifiedRole.forEach(role -> role.delete().queue());

                event.getGuild().createRole().setName("Verified").queue((role) -> {
                    role.getManager().setColor(Color.GREEN).queue();
                    event.getInteraction().getGuild()
                        .getTextChannels().forEach(channel -> {
                            if (!channel.getName().equalsIgnoreCase("verify")) {
                                channel.upsertPermissionOverride(Objects.requireNonNull(event.getGuild().getRoleById(role.getId()))).grant(Permission.VIEW_CHANNEL).queue();
                                channel.upsertPermissionOverride(event.getGuild().getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();
                            } else {
                                channel.upsertPermissionOverride(Objects.requireNonNull(event.getGuild().getRoleById(role.getId()))).deny(Permission.VIEW_CHANNEL).queue();
                            }
                        });
                });
            }

            event.reply("Verification channel has been created").setEphemeral(true).queue();
        }
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        if (event.getChannel().getName().equalsIgnoreCase("verify")) {
            MessageChannel channel = event.getChannel().asMessageChannel();
            channel.sendMessage("Type `verify` to verify yourself").queue();
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        TextChannel verificationChannel = guild.getTextChannelsByName("verify", true).get(0);

        if (!guild.getRolesByName("Unverified", true).isEmpty() && verificationChannel != null) {
            guild.modifyMemberRoles(member, guild.getRolesByName("Unverified", true)).queue();
            verificationChannel.sendMessage("Welcome to the server, " + member.getAsMention() + "! Please type `verify` to verify yourself.").queue();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        List<TextChannel> verificationChannel = event.getGuild().getTextChannelsByName("verify", true);
        if (event.getChannel().asTextChannel().equals(verificationChannel.get(0))) {
            if (event.getMessage().getContentRaw().equals("verify")) {
                Guild guild = event.getGuild();
                Member member = event.getMember();

                if (member != null) {
                    guild.modifyMemberRoles(member, guild.getRolesByName("Verified", true)).queue();
                }

                if (!event.getMessage().getAuthor().isBot()) {
                    verificationChannel.get(0).deleteMessageById(verificationChannel.get(0).getLatestMessageId()).queue();
                }
            }
        }
    }
}
