package org.shithackers.commands.other;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class AvatarCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getFullCommandName().equals("avatar")) {
            OptionMapping optUser = event.getOption("user");
            if (optUser == null) {
                event.getUser();
                event.replyEmbeds(
                    new EmbedBuilder()
                        .setTitle("User avatar")
                        .setAuthor(event.getUser().getAsTag(), null, event.getUser().getAvatarUrl())
                        .setImage(event.getUser().getEffectiveAvatarUrl())
                        .build()
                ).queue();
            } else {
                User user = optUser.getAsUser();
                if (user.getAvatarUrl() != null) {
                    event.replyEmbeds(
                        new EmbedBuilder()
                            .setTitle("User avatar")
                            .setAuthor(user.getAsTag(), null, user.getAvatarUrl())
                            .setImage(user.getEffectiveAvatarUrl())
                            .build()
                    ).queue();
                } else {
                    event.reply("User has no avatar").queue();
                }
            }
        }
    }
}
