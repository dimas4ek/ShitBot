package org.shithackers.commands.mod;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.*;
import java.util.Objects;

public class ReportCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        String username = "postgres";
        String password = "root";
        String url = "jdbc:postgresql://localhost:5432/ShitBot_db";

        Connection connection;

        Guild guild = event.getGuild();

        if (event.getFullCommandName().equals("channel set moderation")) {
            TextChannel modChannel = Objects.requireNonNull(event.getOption("channel")).getAsChannel().asTextChannel();
            try {
                connection = DriverManager.getConnection(url, username, password);

                PreparedStatement st = connection.prepareStatement(
                    "SELECT server_id FROM moderation_channels WHERE server_id = ?");
                st.setString(1, guild.getId());
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    if (rs.getString("server_id").equals(guild.getId())) {
                        st = connection.prepareStatement(
                            "UPDATE moderation_channels SET channel_name = ? WHERE server_id = ?");
                    }
                } else {
                    st = connection.prepareStatement(
                        "INSERT INTO moderation_channels (channel_name, server_id) VALUES (?, ?)");
                }

                st.setString(1, modChannel.getName());
                st.setString(2, guild.getId());
                st.executeUpdate();

                event.reply("Moderation channel set to " + modChannel.getAsMention()).queue();
                event.getChannel().deleteMessageById(event.getChannel().getLatestMessageId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (event.getFullCommandName().equals("report")) {
            String reason = Objects.requireNonNull(event.getOption("reason")).getAsString();
            Member reporter = event.getMember();

            try {
                connection = DriverManager.getConnection(url, username, password);

                PreparedStatement st = connection.prepareStatement(
                    "SELECT channel_name FROM moderation_channels WHERE server_id = ?");
                assert guild != null;
                st.setString(1, guild.getId());
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    TextChannel modChannel = guild.getTextChannelsByName(rs.getString("channel_name"), true).get(0);
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
                st.close();
                rs.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
