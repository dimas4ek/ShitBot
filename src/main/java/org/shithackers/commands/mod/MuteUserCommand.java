package org.shithackers.commands.mod;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.shithackers.utils.ModerUtils;

import java.time.Duration;
import java.util.Objects;

public class MuteUserCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        assert guild != null;

        if (event.getFullCommandName().equals("mute")) {
            User user = Objects.requireNonNull(event.getOption("user")).getAsUser();
            OptionMapping mapping = event.getOption("reason");
            String reason = null;
            if (mapping != null) reason = mapping.getAsString();

            String durationOption = Objects.requireNonNull(event.getOption("duration")).getAsString();
            String duration = new StringBuffer(durationOption).deleteCharAt(durationOption.length() - 1).toString();

            if (durationOption.endsWith("s")) {
                ModerUtils.muteUser(guild, user, Duration.ofSeconds(Integer.parseInt(duration)), reason);
                event.reply("User " + user.getAsMention() + " was muted" + (reason != null ? " for \"" + reason + "\"" : "")).queue();
            }
            if (durationOption.endsWith("m")) {
                ModerUtils.muteUser(guild, user, Duration.ofMinutes(Integer.parseInt(duration)), reason);
                event.reply("User " + user.getAsMention() + " was muted" + (reason != null ? " for \"" + reason + "\"" : "")).queue();
            }
            if (durationOption.endsWith("h")) {
                ModerUtils.muteUser(guild, user, Duration.ofHours(Integer.parseInt(duration)), reason);
                event.reply("User " + user.getAsMention() + " was muted" + (reason != null ? " for \"" + reason + "\"" : "")).queue();
            }
        }

        if (event.getFullCommandName().equals("unmute")) {
            OptionMapping optUser = event.getOption("user");
            User user = null;
            if (optUser != null) user = optUser.getAsUser();

            OptionMapping mapping = event.getOption("reason");
            String reason = null;
            if (mapping != null) reason = mapping.getAsString();

            assert user != null;
            ModerUtils.unmuteUser(guild, user, reason);
            event.reply("User " + user.getAsMention() + " was unmuted" + (reason != null ? " for \"" + reason + "\"" : "")).queue();
        }
    }
}
