package org.shithackers.commands.mod;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jooq.DSLContext;
import org.shithackers.utils.ModerUtils;
import org.shithackers.utils.ShitBotDatabase;

import java.util.Objects;

public class WarnCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        assert guild != null;

        if (event.getFullCommandName().equals("warn user")) {
            DSLContext dslContext = ShitBotDatabase.getDSLContext();
            User user = Objects.requireNonNull(event.getOption("user")).getAsUser();
            String reason = event.getOption("reason") != null ? Objects.requireNonNull(event.getOption("reason")).getAsString() : null;
            ModerUtils.warnUser(guild, user, reason, dslContext, event);
        }

        if (event.getFullCommandName().equals("warn delete")) {
            DSLContext dslContext = ShitBotDatabase.getDSLContext();

            User user = Objects.requireNonNull(event.getOption("user")).getAsUser();
            ModerUtils.deleteWarn(guild, user, dslContext, event);
        }

        if (event.getFullCommandName().equals("warn clear")) {
            DSLContext dslContext = ShitBotDatabase.getDSLContext();

            User user = Objects.requireNonNull(event.getOption("user")).getAsUser();
            ModerUtils.clearWarn(guild, user, dslContext, event);
        }

        if (event.getFullCommandName().equals("warns")) {
            DSLContext dslContext = ShitBotDatabase.getDSLContext();

            User user = Objects.requireNonNull(event.getOption("user")).getAsUser();
            ModerUtils.getWarns(guild, user, dslContext, event);
        }

        if (event.getFullCommandName().equals("warn list")) {
            DSLContext dslContext = ShitBotDatabase.getDSLContext();

            User user = event.getUser();
            ModerUtils.getWarnList(guild, user, dslContext, event);
        }
    }
}
