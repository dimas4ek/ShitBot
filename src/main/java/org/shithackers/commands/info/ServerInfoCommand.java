package org.shithackers.commands.info;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServerInfoCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getFullCommandName().equals("serverinfo")) {
            List<Member> members = event.getGuild().getMembers();
            System.out.println(members);
            int onlineMembers = 0;
            int offlineMembers = 0;
            int idleMembers = 0;
            int dndMembers = 0;
            for (Member member : members) {
                if (member.getOnlineStatus() == OnlineStatus.ONLINE) {
                    onlineMembers++;
                }
                if (member.getOnlineStatus() == OnlineStatus.DO_NOT_DISTURB) {
                    dndMembers++;
                }
                if (member.getOnlineStatus() == OnlineStatus.IDLE) {
                    idleMembers++;
                }
                if (member.getOnlineStatus() == OnlineStatus.OFFLINE) {
                    offlineMembers++;
                }
            }
            System.out.println(offlineMembers);
            List<String> statuses = new ArrayList<>();
            statuses.add(onlineMembers + " online");
            statuses.add(idleMembers + " idle");
            statuses.add(dndMembers + " dnd");
            statuses.add(offlineMembers + " offline");

            event.replyEmbeds(
                new EmbedBuilder()
                    .setTitle("Information of the server " + event.getGuild().getName())
                    .addField("Members", String.valueOf(event.getGuild().getMemberCount()), true)
                    .addField("Statuses", String.join("\n", statuses), true)
                    .addField("Channels", String.valueOf(event.getGuild().getChannels().size()), true)
                    .addField("Server owner", event.getGuild().getOwner().getAsMention(), true)
                    .addField("Privacy level", Objects.requireNonNull(event.getGuild().getVerificationLevel()).toString(), true)
                    .addField("User created", event.getGuild().getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME), true)
                    .setThumbnail(event.getGuild().getIconUrl())
                    .build()
            ).queue();
        }
    }
}
