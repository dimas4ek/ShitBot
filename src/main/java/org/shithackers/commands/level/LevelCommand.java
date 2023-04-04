package org.shithackers.commands.level;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jooq.DSLContext;
import org.shithackers.db.tables.records.UserXpRecord;
import org.shithackers.utils.ShitBotDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.shithackers.db.tables.UserXp.USER_XP;

public class LevelCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getFullCommandName().equals("level show")) {
            DSLContext dslContext = ShitBotDatabase.getDSLContext();

            getLevel(event, dslContext);
        }

        if (event.getFullCommandName().equals("level leaderboard")) {
            DSLContext dslContext = ShitBotDatabase.getDSLContext();

            getLevelLeaderboard(event, dslContext);
        }
    }

    private void getLevelLeaderboard(SlashCommandInteractionEvent event, DSLContext dslContext) {
        List<UserXpRecord> records = dslContext
            .selectFrom(USER_XP)
            .where(USER_XP.SERVER_ID.eq(event.getGuild().getId()))
            .orderBy(USER_XP.TOTAL_XP.desc())
            .limit(10)
            .fetch();

        List<String> places = new ArrayList<>();
        int rank = 1;
        for (UserXpRecord record : records) {
            String member = Objects.requireNonNull(event.getGuild().getMemberById(record.getUserId())).getAsMention();
            String place = String.format("**#%d. %s\n\nLevel: %d | XP: %d**", rank++, member, record.getLevel(), record.getTotalXp());
            places.add(place);
        }

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("Level Leaderboard")
            .setDescription(String.join("\n", places));

        event.replyEmbeds(embed.build()).queue();
    }

    private void getLevel(SlashCommandInteractionEvent event, DSLContext dslContext) {
        UserXpRecord user = dslContext.selectFrom(USER_XP)
            .where(USER_XP.SERVER_ID.eq(event.getGuild().getId()))
            .and(USER_XP.USER_ID.eq(event.getUser().getId()))
            .fetchOne();

        if (user != null) {
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Level")
                .addField("User", event.getUser().getAsMention(), false)
                .addField("Level", String.valueOf(user.getLevel()), true)
                .addField("Level XP", user.getLevelXp() + " / " + Levels.levels.get(user.getLevel() + 1), true);

            event.replyEmbeds(embed.build()).queue();
        } else {
            event.reply("You are not in the database!").queue();
        }
    }
}
