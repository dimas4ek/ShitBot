package org.shithackers.commands.level;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LevelCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getFullCommandName().equals("level show")) {
            String username = "postgres";
            String password = "root";
            String url = "jdbc:postgresql://localhost:5432/ShitBot_db";

            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                getLevel(event, connection);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (event.getFullCommandName().equals("level leaderboard")) {
            String username = "postgres";
            String password = "root";
            String url = "jdbc:postgresql://localhost:5432/ShitBot_db";

            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                getLevelLeaderboard(event, connection);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void getLevelLeaderboard(SlashCommandInteractionEvent event, Connection connection) {
        String query = "SELECT user_id, level, total_xp FROM user_xp WHERE server_id = ? ORDER BY total_xp DESC LIMIT 10";
        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, Objects.requireNonNull(event.getGuild()).getId());

            try (ResultSet rs = st.executeQuery()) {
                List<String> places = new ArrayList<>();
                int rank = 1;
                while (rs.next()) {
                    String member = Objects.requireNonNull(event.getGuild().getMemberById(rs.getString("user_id"))).getAsMention();
                    String place = String.format("**#%d. %s\nLevel: %d | XP: %d**", rank++, member, rs.getInt("level"), rs.getInt("total_xp"));
                    places.add(place);
                }

                EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Level Leaderboard")
                    .setDescription(String.join("\n", places));

                event.replyEmbeds(embed.build()).queue();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void getLevel(SlashCommandInteractionEvent event, Connection connection) throws SQLException {
        String query = "SELECT level, level_xp, total_xp FROM user_xp WHERE server_id = ? AND user_id = ?";
        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, Objects.requireNonNull(event.getGuild()).getId());
            st.setString(2, event.getUser().getId());

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Level")
                        .addField("User", event.getUser().getAsMention(), false)
                        .addField("Level", String.valueOf(rs.getInt("level")), true)
                        .addField("Level XP", rs.getInt("level_xp") + " / " + Levels.levels.get(rs.getInt("level") + 1), true);

                    event.replyEmbeds(embed.build()).queue();
                } else {
                    event.reply("You are not in the database!").queue();
                }
            }
        }
    }
}
