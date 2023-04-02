package org.shithackers.commands.level;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RewardCommand extends ListenerAdapter {
    String level;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getFullCommandName().equals("level reward")) {
            level = Objects.requireNonNull(event.getOption("level")).getAsString();
            String reward = Objects.requireNonNull(event.getOption("reward")).getAsString();

            EntitySelectMenu menu;
            if (reward.equals("role")) {
                menu = EntitySelectMenu.create("role", EntitySelectMenu.SelectTarget.ROLE).build();
                event.reply("Choose role to add").addComponents(ActionRow.of(menu)).setEphemeral(true).queue();
            } else if (reward.equals("channel")) {
                menu = EntitySelectMenu.create("channel", EntitySelectMenu.SelectTarget.CHANNEL).build();
                event.reply("Choose channel to add").addComponents(ActionRow.of(menu)).queue();
            }
        }
        if (event.getFullCommandName().equals("level rewards")) {
            String username = "postgres";
            String password = "root";
            String url = "jdbc:postgresql://localhost:5432/ShitBot_db";
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Rewards");
            List<String> levels = new ArrayList<>();
            List<String> rewards = new ArrayList<>();
            List<String> types = new ArrayList<>();
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                PreparedStatement st = connection.prepareStatement("SELECT level, reward, type FROM level_rewards WHERE server_id = ?");
                st.setString(1, event.getGuild().getId());
                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    levels.add(String.valueOf(rs.getInt("level")));
                    if (rs.getString("type").equals("role")) {
                        rewards.add(event.getGuild().getRoleById(rs.getString("reward")).getAsMention());
                    }
                    if (rs.getString("type").equals("channel")) {
                        rewards.add(event.getGuild().getGuildChannelById(rs.getString("reward")).getAsMention());
                    }
                    types.add(rs.getString("type"));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            builder.addField("Level", String.join("\n\n", levels), true);
            builder.addField("Reward", String.join("\n\n", rewards), true);
            builder.addField("Type", String.join("\n\n", types), true);
            event.replyEmbeds(builder.build()).queue();
        }
    }

    @Override
    public void onEntitySelectInteraction(EntitySelectInteractionEvent event) {
        String username = "postgres";
        String password = "root";
        String url = "jdbc:postgresql://localhost:5432/ShitBot_db";

        String componentId = event.getComponentId();
        String type;
        String rewardId;
        if (componentId.equals("role")) {
            Role role = event.getMentions().getRoles().get(0);
            type = "role";
            rewardId = role.getId();
        } else if (componentId.equals("channel")) {
            Channel channel = event.getMentions().getChannels().get(0);
            type = "channel";
            rewardId = channel.getId();
        } else {
            return;
        }

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String rewardQuery = "SELECT reward FROM level_rewards WHERE server_id = ? AND reward = ?";
            try (PreparedStatement getRewardSt = connection.prepareStatement(rewardQuery)) {
                getRewardSt.setString(1, Objects.requireNonNull(event.getGuild()).getId());
                getRewardSt.setString(2, rewardId);
                ResultSet getRewardRs = getRewardSt.executeQuery();
                if (getRewardRs.next()) {
                    String updateQuery = "UPDATE level_rewards SET level = ? WHERE server_id = ? AND reward = ?";
                    try (PreparedStatement updateRewardSt = connection.prepareStatement(updateQuery)) {
                        updateRewardSt.setInt(1, Integer.parseInt(level));
                        updateRewardSt.setString(2, Objects.requireNonNull(event.getGuild()).getId());
                        updateRewardSt.setString(3, rewardId);
                        updateRewardSt.executeUpdate();
                    }
                } else {
                    String insertQuery = "INSERT INTO level_rewards (server_id, level, reward, type) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement setRewardSt = connection.prepareStatement(insertQuery)) {
                        setRewardSt.setString(1, Objects.requireNonNull(event.getGuild()).getId());
                        setRewardSt.setInt(2, Integer.parseInt(level));
                        setRewardSt.setString(3, rewardId);
                        setRewardSt.setString(4, type);
                        setRewardSt.executeUpdate();
                    }
                }
            }

            try (
                PreparedStatement getRewardSt =
                    connection.prepareStatement("SELECT level, reward, type FROM level_rewards WHERE server_id = ? AND type = ?");
                PreparedStatement getUserSt =
                    connection.prepareStatement("SELECT level FROM user_xp WHERE server_id = ? and user_id = ?")
            ) {
                getRewardSt.setString(1, Objects.requireNonNull(event.getGuild()).getId());
                getRewardSt.setString(2, type);
                ResultSet getRewardRs = getRewardSt.executeQuery();
                List<Member> members = event.getGuild().getMembers();
                while (getRewardRs.next()) {
                    for (Member member : members) {
                        getUserSt.setString(1, event.getGuild().getId());
                        getUserSt.setString(2, member.getId());
                        ResultSet getUserRs = getUserSt.executeQuery();
                        while (getUserRs.next()) {
                            if (getUserRs.getInt("level") < getRewardRs.getInt("level")) {
                                if (type.equals("role")) {
                                    String roleId = getRewardRs.getString("reward");
                                    Role role = event.getGuild().getRoleById(roleId);
                                    assert role != null;
                                    event.getGuild().removeRoleFromMember(member, role).queue();
                                }
                                if (type.equals("channel")) {
                                    Objects.requireNonNull(event.getGuild().getGuildChannelById(getRewardRs.getString("reward")))
                                        .getPermissionContainer().upsertPermissionOverride(member).clear(Permission.VIEW_CHANNEL).queue();
                                }
                            } else if (getUserRs.getInt("level") >= getRewardRs.getInt("level")){
                                if (type.equals("role")) {
                                    Role rewardRole = event.getGuild().getRoleById(getRewardRs.getString("reward"));
                                    if (rewardRole != null && !member.getRoles().contains(rewardRole)) {
                                        event.getGuild().addRoleToMember(member, rewardRole).queue();
                                    }
                                }
                                if (type.equals("channel")) {
                                    Objects.requireNonNull(event.getGuild().getGuildChannelById(getRewardRs.getString("reward")))
                                        .getPermissionContainer().upsertPermissionOverride(member).grant(Permission.VIEW_CHANNEL).queue();
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String rewardMention = null;
        if (type.equals("role")) {
            rewardMention = event.getMentions().getRoles().get(0).getAsMention();
        }
        if (type.equals("channel")) {
            rewardMention = event.getMentions().getChannels().get(0).getAsMention();
        }

        event.deferEdit().delay(10, TimeUnit.SECONDS).flatMap(InteractionHook::deleteOriginal).queue();
        //event.reply("Reward " + rewardMention + " has been successfully added to level " + level).setEphemeral(true).queue();
    }
}
