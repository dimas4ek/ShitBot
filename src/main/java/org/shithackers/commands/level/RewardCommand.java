package org.shithackers.commands.level;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

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
                event.reply("Choose channel to add").addComponents(ActionRow.of(menu)).setEphemeral(true).queue();
            }
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
            String query = "INSERT INTO level_rewards (server_id, level, reward, type) VALUES (?, ?, ?, ?)";
            try (PreparedStatement st = connection.prepareStatement(query)) {
                st.setString(1, Objects.requireNonNull(event.getGuild()).getId());
                st.setInt(2, Integer.parseInt(level));
                st.setString(3, rewardId);
                st.setString(4, type);
                st.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String rewardMention;
        if (type.equals("role")) {
            rewardMention = event.getMentions().getRoles().get(0).getAsMention();
        } else {
            rewardMention = event.getMentions().getChannels().get(0).getAsMention();
        }
        event.reply("Reward " + rewardMention + " has been successfully added to level " + level).setEphemeral(true).queue();
    }
}
