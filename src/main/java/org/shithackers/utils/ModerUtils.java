package org.shithackers.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    public static void warnUser(Guild guild, User user, String reason, Connection connection, SlashCommandInteractionEvent event) {
        try {
            PreparedStatement st = connection.prepareStatement(
                "SELECT server_id, user_id FROM warnings WHERE server_id = ? and user_id = ?");
            st.setString(1, guild.getId());
            st.setString(2, user.getId());
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                if (rs.getString("server_id").equals(guild.getId()) && rs.getString("user_id").equals(user.getId())) {
                    st = connection.prepareStatement(
                        "UPDATE warnings SET warn_count = warn_count + 1 WHERE server_id = ? and user_id = ?");
                    st.setString(1, guild.getId());
                    st.setString(2, user.getId());
                }
            } else {
                st = connection.prepareStatement(
                    "INSERT INTO warnings (server_id, user_id, warn_count) VALUES (?, ?, ?)");
                st.setString(1, guild.getId());
                st.setString(2, user.getId());
                st.setInt(3, 1);
            }
            st.executeUpdate();
            
            event.reply("User " + user.getAsMention() + " was warned" + (reason != null ? " for \"" + reason + "\"" : "")).queue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteWarn(Guild guild, User user, Connection connection, SlashCommandInteractionEvent event) {
        try {
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
        } catch (
            SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void clearWarn(Guild guild, User user, Connection connection, SlashCommandInteractionEvent event) {
        try {
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
        } catch (
            SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void getWarns(Guild guild, User user, Connection connection, SlashCommandInteractionEvent event) {
        try {
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
        } catch (
            SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void getWarnList(Guild guild, User user, Connection connection, SlashCommandInteractionEvent event) {
        try {
            List<String> list = new ArrayList<>();
            PreparedStatement st;
            st = connection.prepareStatement(
                "select count(user_id) from warnings"
            );
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

            event.replyEmbeds(embed.build()).queue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addRole(Guild guild, User user, Role role, String reason) {
        guild.addRoleToMember(user, role).queue();
    }
}
