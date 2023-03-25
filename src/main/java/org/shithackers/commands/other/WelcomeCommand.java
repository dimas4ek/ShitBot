package org.shithackers.commands.other;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.*;

public class WelcomeCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String username = "postgres";
        String password = "root";
        String url = "jdbc:postgresql://localhost:5432/ShitBot_db";

        Connection connection;
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (event.getFullCommandName().equals("channel set welcome")) {
            Guild guild = event.getGuild();
            assert guild != null;

            String channelName = event.getOption("channel").getAsChannel().getName();
            TextChannel textChannel = guild.getTextChannelsByName(channelName, true).get(0);
            if (textChannel != null) {
                try {
                    PreparedStatement st = connection.prepareStatement(
                        "SELECT server_id FROM welcome_channels WHERE server_id = ?");
                    st.setString(1, guild.getId());
                    ResultSet rs = st.executeQuery();
                    if (rs.next()) {
                        if (rs.getString("server_id").equals(guild.getId())) {
                            st = connection.prepareStatement(
                                "UPDATE welcome_channels SET channel_name = ? WHERE server_id = ?");
                        }
                    } else {
                        st = connection.prepareStatement(
                            "INSERT INTO welcome_channels (channel_name, server_id) VALUES (?, ?)");
                    }

                    st.setString(1, channelName);
                    st.setString(2, guild.getId());
                    st.executeUpdate();

                    event.reply("Welcome channel set to " + textChannel.getAsMention()).setEphemeral(true).queue();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                event.reply("Channel not found").queue();
            }
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        String username = "postgres";
        String password = "root";
        String url = "jdbc:postgresql://localhost:5432/ShitBot_db";

        Connection connection;
        try {
            connection = DriverManager.getConnection(url, username, password);
            PreparedStatement st = connection.prepareStatement(
                "SELECT channel_name FROM welcome_channels WHERE server_id = ?");
            st.setString(1, event.getGuild().getId());

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                String channelName = rs.getString("channel_name");
                event.getGuild().getTextChannelsByName(channelName, true).forEach(channel -> {
                    channel.sendMessage(event.getMember().getAsMention() + " has joined " + event.getGuild().getName() + "!").queue();
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        String username = "postgres";
        String password = "root";
        String url = "jdbc:postgresql://localhost:5432/ShitBot_db";

        Connection connection;
        try {
            connection = DriverManager.getConnection(url, username, password);
            PreparedStatement st = connection.prepareStatement(
                "SELECT channel_name FROM welcome_channels WHERE server_id = ?");
            st.setString(1, event.getGuild().getId());

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                String channelName = rs.getString("channel_name");
                event.getGuild().getTextChannelsByName(channelName, true).forEach(channel -> {
                    channel.sendMessage(event.getMember().getAsMention() + " has left " + event.getGuild().getName() + "!").queue();
                });

            }
        } catch (
            SQLException e) {
            e.printStackTrace();
        }
    }
}
