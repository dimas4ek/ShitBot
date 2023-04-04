package org.shithackers.commands.mod;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jooq.DSLContext;
import org.shithackers.db.tables.records.ModerationChannelsRecord;
import org.shithackers.utils.ShitBotDatabase;

import java.util.Objects;

import static org.shithackers.db.Tables.MODERATION_CHANNELS;

public class ReportCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getFullCommandName().equals("channel set moderation")) {
            TextChannel modChannel = Objects.requireNonNull(event.getOption("channel")).getAsChannel().asTextChannel();

            DSLContext dslContext = ShitBotDatabase.getDSLContext();

            ModerationChannelsRecord moderationChannel = dslContext.selectFrom(MODERATION_CHANNELS)
                .where(MODERATION_CHANNELS.SERVER_ID.eq(event.getGuild().getId()))
                .fetchOne();

            if (moderationChannel != null) {
                dslContext.update(MODERATION_CHANNELS)
                    .set(MODERATION_CHANNELS.CHANNEL_NAME, modChannel.getName())
                    .where(MODERATION_CHANNELS.SERVER_ID.eq(event.getGuild().getId()))
                    .execute();
            } else {
                dslContext.insertInto(MODERATION_CHANNELS)
                    .set(MODERATION_CHANNELS.CHANNEL_NAME, modChannel.getName())
                    .set(MODERATION_CHANNELS.SERVER_ID, event.getGuild().getId())
                    .execute();
            }

            event.reply("Moderation channel set to " + modChannel.getAsMention()).setEphemeral(true).queue();

        }

        if (event.getFullCommandName().equals("report")) {
            String reason = Objects.requireNonNull(event.getOption("reason")).getAsString();
            Member reporter = event.getMember();

            DSLContext dslContext = ShitBotDatabase.getDSLContext();

            ModerationChannelsRecord moderationChannel = dslContext.selectFrom(MODERATION_CHANNELS)
                .where(MODERATION_CHANNELS.SERVER_ID.eq(event.getGuild().getId()))
                .fetchOne();

            if (moderationChannel != null) {
                TextChannel modChannel = event.getGuild().getTextChannelsByName(moderationChannel.getChannelName(), true).get(0);
                if (modChannel != null) {
                    assert reporter != null;
                    EmbedBuilder builder = new EmbedBuilder()
                        .setTitle("Report")
                        .addField("Reporter", reporter.getAsMention(), false)
                        .addField("Reason", reason, false)
                        .setFooter("Reported by " + reporter.getNickname(), reporter.getAvatarUrl());

                    modChannel.sendMessageEmbeds(builder.build()).queue();

                    event.reply("Thank you for your report!\nThe moderation team will look into it as soon as possible.").queue();
                } else {
                    event.reply("Moderation channel not found").queue();
                }
            }
        }
    }
}
