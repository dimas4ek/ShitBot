package org.shithackers.commands.mod;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.shithackers.utils.ModerUtils;

import java.sql.*;
import java.util.Objects;

public class WarnCommand extends ListenerAdapter {

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

        Guild guild = event.getGuild();
        assert guild != null;

        if (event.getFullCommandName().equals("warn")) {
            User user = Objects.requireNonNull(event.getOption("user")).getAsUser();
            String reason = event.getOption("reason") != null ? Objects.requireNonNull(event.getOption("reason")).getAsString() : null;
            ModerUtils.warnUser(guild, user, reason, connection, event);
        }

        if (event.getFullCommandName().equals("warn delete")) {
            User user = Objects.requireNonNull(event.getOption("user")).getAsUser();
            ModerUtils.deleteWarn(guild, user, connection, event);
        }

        if (event.getFullCommandName().equals("warn clear")) {
            User user = Objects.requireNonNull(event.getOption("user")).getAsUser();
            ModerUtils.clearWarn(guild, user, connection, event);
        }

        if(event.getFullCommandName().equals("warns")) {
            User user = Objects.requireNonNull(event.getOption("user")).getAsUser();
            ModerUtils.getWarns(guild, user, connection, event);
        }

        if(event.getFullCommandName().equals("warn list")) {
            User user = event.getUser();
            ModerUtils.getWarnList(guild, user, connection, event);
        }
    }
}
