package org.shithackers.commands.mod;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.shithackers.utils.ModerUtils;

import java.util.Objects;

public class BanUserCommand extends ListenerAdapter {


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        assert guild != null;

        if(event.getFullCommandName().equals("ban")) {
            User user = Objects.requireNonNull(event.getOption("user")).getAsUser();
            OptionMapping optReason = event.getOption("reason");
            String reason = null;
            if (optReason != null) reason = optReason.getAsString();

            ModerUtils.banUser(guild, user, reason);
            event.reply("User " + user.getAsMention() + " was banned for " + (reason != null ? " for \"" + reason + "\"" : "")).queue();
        }
        if(event.getFullCommandName().equals("unban")) {
            User user = Objects.requireNonNull(event.getOption("user")).getAsUser();
            OptionMapping mapping = event.getOption("reason");
            String reason = null;
            if (mapping != null) reason = mapping.getAsString();

            ModerUtils.unbanUser(guild, user, reason);
            event.reply("User " + user.getAsMention() + " was unbanned for " + (reason != null ? " for \"" + reason + "\"" : "")).queue();
        }
    }
}
