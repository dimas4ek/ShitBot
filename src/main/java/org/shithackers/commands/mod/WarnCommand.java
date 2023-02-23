package org.shithackers.commands.mod;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.shithackers.utils.ModerUtils;

import java.sql.*;

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

        if(event.getFullCommandName().equals("warn")) {
            User user = event.getOption("user").getAsUser();
            OptionMapping mapping = event.getOption("reason");
            String reason = null;
            if (mapping != null) reason = mapping.getAsString();

            ModerUtils.warnUser(guild, user, reason, connection, event);
        }

        if(event.getFullCommandName().equals("unwarn")) {
            User user = event.getOption("user").getAsUser();
            OptionMapping mapping = event.getOption("reason");
            String reason = null;
            if (mapping != null) reason = mapping.getAsString();

            ModerUtils.deleteWarn(guild, user, connection, event);
        }

        if (event.getFullCommandName().equals("clearwarns")) {
            User user = event.getOption("user").getAsUser();
            OptionMapping mapping = event.getOption("reason");
            String reason = null;
            if (mapping != null) reason = mapping.getAsString();

            ModerUtils.clearWarn(guild, user, connection, event);
        }

        if(event.getFullCommandName().equals("warns")) {
            User user = event.getOption("user").getAsUser();
            ModerUtils.getWarns(guild, user, connection, event);
        }

        if(event.getFullCommandName().equals("warnlist")) {
            User user = event.getUser();
            ModerUtils.getWarnList(guild, user, connection, event);
        }
    }
}
