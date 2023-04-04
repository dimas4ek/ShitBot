package org.shithackers.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Result;
import org.shithackers.db.tables.records.WarningsRecord;

import java.awt.*;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.shithackers.db.tables.Warnings.WARNINGS;

public class ModerUtils {
    public static void deleteMessage(Message message, String reason) {
        message.delete().reason(reason).queue();
    }

    public static void banUser(Guild guild, User user, String reason) {
        guild.ban(user, 0, TimeUnit.DAYS).reason(reason).queue();
    }

    public static void unbanUser(Guild guild, User user, String reason) {
        guild.unban(user).reason(reason).queue();
    }

    public static void kickUser(Guild guild, User user, String reason) {
        guild.kick(user).reason(reason).queue();
    }

    public static void muteUser(Guild guild, User user, Duration duration, String reason) {
        guild.timeoutFor(user, duration).reason(reason).queue();
    }

    public static void unmuteUser(Guild guild, User user, String reason) {
        guild.removeTimeout(user).reason(reason).queue();
    }

    public static void warnUser(Guild guild, User user, String reason, DSLContext dslContext, SlashCommandInteractionEvent event) {
        boolean warnExists = dslContext.selectFrom(WARNINGS)
            .where(WARNINGS.SERVER_ID.eq(guild.getId()))
            .and(WARNINGS.USER_ID.eq(user.getId()))
            .fetchOne() != null;

        if (warnExists) {
            dslContext.update(WARNINGS)
                .set(WARNINGS.WARN_COUNT, WARNINGS.WARN_COUNT.plus(1))
                .where(WARNINGS.SERVER_ID.eq(event.getGuild().getId())
                    .and(WARNINGS.USER_ID.eq(user.getId())))
                .execute();
        } else {
            dslContext.insertInto(WARNINGS)
                .set(WARNINGS.SERVER_ID, guild.getId())
                .set(WARNINGS.USER_ID, user.getId())
                .set(WARNINGS.WARN_COUNT, 1)
                .execute();
        }

        event.reply("User " + user.getAsMention() + " was warned" + (reason != null ? " for \"" + reason + "\"" : "")).queue();
    }

    public static void deleteWarn(Guild guild, User user, DSLContext dslContext, SlashCommandInteractionEvent event) {
        Result<WarningsRecord> result =
            dslContext
                .selectFrom(WARNINGS)
                .where(WARNINGS.SERVER_ID.eq(guild.getId())
                    .and(WARNINGS.USER_ID.eq(user.getId())))
                .fetch();

        if (result.isEmpty()) {
            event.reply("User " + user.getAsMention() + " has no warnings").queue();
        } else {
            WarningsRecord record = result.get(0);
            int warnCount = record.getWarnCount();
            if (warnCount == 1) {
                dslContext.deleteFrom(WARNINGS)
                    .where(WARNINGS.SERVER_ID.eq(guild.getId())
                        .and(WARNINGS.USER_ID.eq(user.getId())))
                    .execute();

                event.reply("User " + user.getAsMention() + " has had their last warning removed").queue();
            } else {
                dslContext.update(WARNINGS)
                    .set(WARNINGS.WARN_COUNT, WARNINGS.WARN_COUNT.minus(1))
                    .where(WARNINGS.SERVER_ID.eq(guild.getId())
                        .and(WARNINGS.USER_ID.eq(user.getId())))
                    .execute();

                event.reply("User " + user.getAsMention() + " has been unwarned").queue();
            }
        }

        /*try {
            PreparedStatement st = connection.prepareStatement(
                "SELECT server_id, user_id FROM warnings WHERE server_id = ? and user_id = ?");
            st.setString(1, guild.getId());
            st.setString(2, user.getId());
            ResultSet rs = st.executeQuery();
            if (!rs.next()) {
                event.reply("User " + user.getAsMention() + " has no warnings").queue();
            } else {
                if (rs.getInt("warn_count") == 1) {
                    st = connection.prepareStatement(
                        "delete from warnings where server_id = ? and user_id = ?");

                    event.reply("User " + user.getAsMention() + "  has had their last warning removed").queue();
                } else {
                    st = connection.prepareStatement(
                        "UPDATE warnings SET warn_count = warn_count - 1 WHERE server_id = ? and user_id = ?");

                    event.reply("User " + user.getAsMention() + " has been unwarned").queue();
                }
                st.setString(1, guild.getId());
                st.setString(2, user.getId());
                st.executeUpdate();
            }

            st.close();
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }*/
    }

    public static void clearWarn(Guild guild, User user, DSLContext dslContext, SlashCommandInteractionEvent event) {
        Result<WarningsRecord> result = dslContext.selectFrom(WARNINGS)
            .where(WARNINGS.SERVER_ID.eq(guild.getId())
                .and(WARNINGS.USER_ID.eq(user.getId())))
            .fetch();

        if (result.isEmpty()) {
            event.reply("User " + user.getAsMention() + " has no warnings").queue();
        } else {
            dslContext.deleteFrom(WARNINGS)
                .where(WARNINGS.SERVER_ID.eq(guild.getId())
                    .and(WARNINGS.USER_ID.eq(user.getId())))
                .execute();

            event.reply("User " + user.getAsMention() + "  has had all their warnings removed").queue();
        }
        /*try {
            PreparedStatement st = connection.prepareStatement(
                "SELECT server_id, user_id FROM warnings WHERE server_id = ? and user_id = ?");
            st.setString(1, guild.getId());
            st.setString(2, user.getId());
            ResultSet rs = st.executeQuery();
            if (!rs.next()) {
                event.reply("User " + user.getAsMention() + " has no warnings").queue();
            } else {
                st = connection.prepareStatement(
                    "delete from warnings where server_id = ? and user_id = ?");

                st.setString(1, guild.getId());
                st.setString(2, user.getId());
                st.executeUpdate();

                event.reply("User " + user.getAsMention() + "  has had all their warnings removed").queue();
            }

            st.close();
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }*/
    }

    public static void getWarns(Guild guild, User user, DSLContext dslContext, SlashCommandInteractionEvent event) {
        Result<Record1<Integer>> result = dslContext.select(WARNINGS.WARN_COUNT)
            .from(WARNINGS)
            .where(WARNINGS.SERVER_ID.eq(guild.getId())
                .and(WARNINGS.USER_ID.eq(user.getId())))
            .fetch();

        if (result.isEmpty()) {
            event.reply("User " + user.getAsMention() + " has no warnings").queue();
        } else {
            int warnCount = result.get(0).component1();
            event.reply("User " + user.getAsMention() + " has " + warnCount + " warnings").queue();
        }
        /*try {
            PreparedStatement st = connection.prepareStatement(
                "SELECT warn_count FROM warnings WHERE server_id = ? and user_id = ?");
            st.setString(1, guild.getId());
            st.setString(2, user.getId());
            ResultSet rs = st.executeQuery();
            if (!rs.next()) {
                event.reply("User " + user.getAsMention() + " has no warnings").queue();
            } else {
                event.reply("User " + user.getAsMention() + " has " + rs.getInt("warn_count") + " warnings").queue();
            }

            st.close();
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }*/
    }

    public static void getWarnList(Guild guild, User user, DSLContext dslContext, SlashCommandInteractionEvent event) {
        Result<Record2<String, Integer>> result =
            dslContext
                .select(WARNINGS.USER_ID, WARNINGS.WARN_COUNT)
                .from(WARNINGS)
                .where(WARNINGS.SERVER_ID.eq(guild.getId()))
                .orderBy(WARNINGS.WARN_COUNT.desc())
                .limit(10)
                .fetch();

        List<String> list = result.stream()
            .map(record -> String.format("<@%s>, warnings: %d", record.get(WARNINGS.USER_ID), record.get(WARNINGS.WARN_COUNT)))
            .collect(Collectors.toList());

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("List of 10 users with the most warnings")
            .setDescription(String.join("\n", list))
            .setColor(Color.RED)
            .setFooter("Requested by " + user.getName(), user.getAvatarUrl());

        event.replyEmbeds(embed.build()).queue();

        /*try {
            List<String> list = new ArrayList<>();

            PreparedStatement st = connection.prepareStatement("select count(user_id) from warnings");

            ResultSet getCount = st.executeQuery();
            getCount.next();
            st = connection.prepareStatement(
                "SELECT user_id, warn_count from warnings WHERE server_id = ?");
            st.setString(1, guild.getId());
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add("<@" + rs.getString("user_id") + ">, warnings: " + rs.getInt("warn_count"));
            }
            list.sort(Collections.reverseOrder());

            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("List of 10 users with the most warnings")
                .setDescription(String.join("\n", list))
                .setColor(Color.RED)
                .setFooter("Requested by " + user.getName(), user.getAvatarUrl());

            st.close();
            rs.close();

            event.replyEmbeds(embed.build()).queue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }*/
    }

    public static void addRole(Guild guild, User user, Role role, String reason) {
        guild.addRoleToMember(user, role).queue();
    }
}
