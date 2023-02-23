package org.shithackers.listeners;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.*;

public class UserJoinLeaveListener extends ListenerAdapter {
    String username = "postgres";
    String password = "root";
    String url = "jdbc:postgresql://localhost:5432/ShitBot_db";

    Connection connection;

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
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
