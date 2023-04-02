package org.shithackers.commands.level;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LevelSystem extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromGuild() && !event.getAuthor().isBot()) {
            User user = event.getAuthor();
            processXpGain(event, user.getId(), event.getGuild().getId());
        }
    }

    private void processXpGain(MessageReceivedEvent event, String userId, String guildId) {
        String username = "postgres";
        String password = "root";
        String url = "jdbc:postgresql://localhost:5432/ShitBot_db";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            updateXp(event, connection, userId, guildId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateXp(MessageReceivedEvent event, Connection connection, String userId, String guildId) throws SQLException {
        String query = "SELECT level_xp, level FROM user_xp WHERE server_id = ? AND user_id = ?";
        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, guildId);
            st.setString(2, userId);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    addXP(connection, guildId, userId);
                    updateLevel(event, connection, userId, guildId);
                } else {
                    addUserToLeveling(connection, userId, guildId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addXP(Connection connection, String guildId, String userId) throws SQLException {
        String query = "UPDATE user_xp SET level_xp = user_xp.level_xp + 1, total_xp = total_xp + 1 WHERE server_id = ? AND user_id = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, guildId);
            st.setString(2, userId);

            st.executeUpdate();
        }
    }

    public void updateLevel(MessageReceivedEvent event, Connection connection, String userId, String guildId) throws SQLException {
        String getXPQuery = "SELECT level_xp, level FROM user_xp WHERE server_id = ? AND user_id = ?";
        String updateLevelQuery = "UPDATE user_xp SET level_xp = ?, level = ? WHERE server_id = ? AND user_id = ?";

        PreparedStatement getXPSt = connection.prepareStatement(getXPQuery);
        PreparedStatement updateLevelSt = connection.prepareStatement(updateLevelQuery);
        getXPSt.setString(1, guildId);
        getXPSt.setString(2, userId);

        ResultSet rs = getXPSt.executeQuery();
        rs.next();
        int currentXP = rs.getInt("level_xp");
        int currentLevel = rs.getInt("level");

        int newLevel = currentLevel;
        for (int i = currentLevel + 1; i <= Levels.levels.size(); i++) {
            if (currentXP >= Levels.getXPForLevel(i)) {
                newLevel = i;
            } else {
                break;
            }
        }

        if (newLevel > currentLevel) {
            updateLevelSt.setInt(1, 0);
            updateLevelSt.setInt(2, newLevel);
            updateLevelSt.setString(3, guildId);
            updateLevelSt.setString(4, userId);
            updateLevelSt.executeUpdate();

            event.getChannel().sendMessage(Objects.requireNonNull(event.getGuild().getMemberById(userId)).getAsMention() + " leveled up to " + newLevel).queue();
        }

        checkRewards(connection, event.getGuild(), userId, guildId, newLevel);
    }

    private void checkRewards(Connection connection, Guild guild, String userId, String guildId, int newLevel) throws SQLException {
        String checkRewardQuery = "SELECT level, reward, type FROM level_rewards WHERE server_id = ?";
        PreparedStatement checkRewardsSt = connection.prepareStatement(checkRewardQuery);
        checkRewardsSt.setString(1, guildId);
        ResultSet rs = checkRewardsSt.executeQuery();
        while (rs.next()) {
            int rewardLevel = rs.getInt("level");
            if (newLevel == rewardLevel) {
                Member member = guild.getMemberById(userId);
                if (rs.getString("type").equals("role")) {
                    Role rewardRole = guild.getRoleById(rs.getString("reward"));
                    if (rewardRole != null && member != null && !member.getRoles().contains(rewardRole)) {
                        guild.addRoleToMember(member, rewardRole).queue();
                    }
                }
                if (rs.getString("type").equals("channel")) {
                    assert member != null;
                    Objects.requireNonNull(guild.getGuildChannelById(rs.getString("reward")))
                        .getPermissionContainer().upsertPermissionOverride(member).grant(Permission.VIEW_CHANNEL).queue();
                }
            }
        }
    }

    private void addUserToLeveling(Connection connection, String userId, String guildId) throws SQLException {
        PreparedStatement st = connection.prepareStatement(
            "INSERT INTO user_xp (server_id, user_id, level_xp, total_xp, level) VALUES (?, ?, ?, ?, ?)");
        st.setString(1, guildId);
        st.setString(2, userId);
        st.setInt(3, 1);
        st.setInt(4, 1);
        st.setInt(5, 0);

        st.executeUpdate();
    }
}
