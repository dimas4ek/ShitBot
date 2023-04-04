package org.shithackers.commands.other;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jooq.DSLContext;
import org.shithackers.db.Tables;
import org.shithackers.db.tables.records.WelcomeChannelsRecord;
import org.shithackers.utils.ShitBotDatabase;

import java.util.Objects;

import static org.shithackers.db.tables.WelcomeChannels.WELCOME_CHANNELS;

public class WelcomeCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getFullCommandName().equals("channel set welcome")) {
            DSLContext dslContext = ShitBotDatabase.getDSLContext();

            setWelcomeChannel(event, dslContext);
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        DSLContext dslContext = ShitBotDatabase.getDSLContext();

        WelcomeChannelsRecord welcomeChannel = dslContext.selectFrom(Tables.WELCOME_CHANNELS)
            .where(Tables.WELCOME_CHANNELS.SERVER_ID.eq(event.getGuild().getId()))
            .fetchOne();

        if (welcomeChannel != null) {
            String channelName = welcomeChannel.getChannelName();
            event.getGuild().getTextChannelsByName(channelName, true).forEach(channel -> {
                channel.sendMessage(event.getMember().getAsMention() + " has joined " + event.getGuild().getName() + "!").queue();
            });
        }
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        DSLContext dslContext = ShitBotDatabase.getDSLContext();

        WelcomeChannelsRecord welcomeChannel = dslContext.selectFrom(Tables.WELCOME_CHANNELS)
            .where(Tables.WELCOME_CHANNELS.SERVER_ID.eq(event.getGuild().getId()))
            .fetchOne();

        if (welcomeChannel != null) {
            String channelName = welcomeChannel.getChannelName();
            Guild guild = event.getGuild();
            Member member = event.getMember();

            if (member != null) {
                guild.getTextChannelsByName(channelName, true)
                    .forEach(channel ->
                        channel.sendMessage(member.getAsMention() + " has left " + guild.getName() + "!").queue());
            }
        }
    }

    private static void setWelcomeChannel(SlashCommandInteractionEvent event, DSLContext dslContext) {
        Guild guild = event.getGuild();
        assert guild != null;

        TextChannel channel = Objects.requireNonNull(event.getOption("channel")).getAsChannel().asTextChannel();

        WelcomeChannelsRecord welcomeChannel = dslContext.selectFrom(WELCOME_CHANNELS)
            .where(WELCOME_CHANNELS.SERVER_ID.eq(guild.getId()))
            .fetchOne();

        if (welcomeChannel != null) {
            dslContext.update(WELCOME_CHANNELS)
                .set(WELCOME_CHANNELS.CHANNEL_NAME, channel.getName())
                .where(WELCOME_CHANNELS.SERVER_ID.eq(guild.getId()))
                .execute();
        } else {
            dslContext.insertInto(WELCOME_CHANNELS)
                .set(WELCOME_CHANNELS.CHANNEL_NAME, channel.getName())
                .set(WELCOME_CHANNELS.SERVER_ID, guild.getId())
                .execute();
        }

        event.reply("Welcome channel set to " + channel.getAsMention()).setEphemeral(true).queue();
    }
}
