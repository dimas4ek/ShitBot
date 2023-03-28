package org.shithackers.commands.info;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.format.DateTimeFormatter;
import java.util.*;

public class ServerInfoCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getFullCommandName().equals("server")) {
            List<Member> members = Objects.requireNonNull(event.getGuild()).getMembers();
            Map<OnlineStatus, Integer> statusCounts = new HashMap<>();
            for (Member member : members) {
                OnlineStatus onlineStatus = member.getOnlineStatus();
                statusCounts.merge(onlineStatus, 1, Integer::sum);
            }
            List<String> statuses = new ArrayList<>();
            for (Map.Entry<OnlineStatus, Integer> entry : statusCounts.entrySet()) {
                String statusString = entry.getValue() + " " + entry.getKey().name().toLowerCase();
                if (entry.getKey() == OnlineStatus.DO_NOT_DISTURB) statuses.add(entry.getValue() + " dnd");
                else statuses.add(statusString);
            }

            event.replyEmbeds(
                new EmbedBuilder()
                    .setTitle("Information of the server " + event.getGuild().getName())
                    .addField("Members", String.valueOf(event.getGuild().getMemberCount()), true)
                    .addField("Statuses", String.join("\n", statuses), true)
                    .addField("Channels", String.valueOf(event.getGuild().getChannels().size()), true)
                    .addField("Server owner", Objects.requireNonNull(event.getGuild().getOwner()).getAsMention(), true)
                    .addField("Privacy level", Objects.requireNonNull(event.getGuild().getVerificationLevel()).toString(), true)
                    .addField("User created", event.getGuild().getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME), true)
                    .setThumbnail(event.getGuild().getIconUrl())
                    .build()
            ).queue();
        }
    }
}
