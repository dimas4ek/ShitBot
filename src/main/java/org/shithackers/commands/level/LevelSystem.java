package org.shithackers.commands.level;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jooq.DSLContext;
import org.shithackers.db.tables.records.LevelRewardsRecord;
import org.shithackers.db.tables.records.UserXpRecord;
import org.shithackers.utils.ShitBotDatabase;

import java.util.List;
import java.util.Objects;

import static org.shithackers.db.tables.LevelRewards.LEVEL_REWARDS;
import static org.shithackers.db.tables.UserXp.USER_XP;

public class LevelSystem extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromGuild() && !event.getAuthor().isBot()) {
            DSLContext dslContext = ShitBotDatabase.getDSLContext();

            String userId = event.getAuthor().getId();
            String serverId = event.getGuild().getId();

            processXPGain(event, dslContext, userId, serverId);
        }
    }

    private void processXPGain(MessageReceivedEvent event, DSLContext dslContext, String userId, String guildId) {
        UserXpRecord user = dslContext
            .selectFrom(USER_XP)
            .where(USER_XP.SERVER_ID.eq(guildId).and(USER_XP.USER_ID.eq(userId)))
            .fetchOne();

        if (user != null) {
            addXP(dslContext, guildId, userId);
            updateLevel(event, dslContext, userId, guildId);
        } else {
            addUserToLeveling(dslContext, userId, guildId);
        }
    }

    public void addXP(DSLContext dslContext, String guildId, String userId) {
        dslContext
            .update(USER_XP)
            .set(USER_XP.LEVEL_XP, USER_XP.LEVEL_XP.plus(1))
            .set(USER_XP.TOTAL_XP, USER_XP.TOTAL_XP.plus(1))
            .where(USER_XP.SERVER_ID.eq(guildId).and(USER_XP.USER_ID.eq(userId)))
            .execute();
    }

    public void updateLevel(MessageReceivedEvent event, DSLContext dslContext, String userId, String guildId) {
        UserXpRecord user = dslContext
            .selectFrom(USER_XP)
            .where(USER_XP.SERVER_ID.eq(guildId).and(USER_XP.USER_ID.eq(userId)))
            .fetchOne();

        assert user != null;
        int currentXP = user.getLevelXp();
        int currentLevel = user.getLevel();

        int newLevel = currentLevel;
        for (int i = currentLevel + 1; i <= Levels.levels.size(); i++) {
            if (currentXP >= Levels.getXPForLevel(i)) {
                newLevel = i;
            } else {
                break;
            }
        }

        if (newLevel > currentLevel) {
            dslContext
                .update(USER_XP)
                .set(USER_XP.LEVEL_XP, 0)
                .set(USER_XP.LEVEL, newLevel)
                .where(USER_XP.SERVER_ID.eq(guildId).and(USER_XP.USER_ID.eq(userId)))
                .execute();

            event.getChannel().sendMessage(Objects.requireNonNull(event.getGuild().getMemberById(userId)).getAsMention() + " leveled up to " + newLevel).queue();
        }

        checkRewards(dslContext, event.getGuild(), userId, guildId, newLevel);
    }

    private void checkRewards(DSLContext dslContext, Guild guild, String userId, String guildId, int newLevel) {
        List<LevelRewardsRecord> rewards = dslContext
            .selectFrom(LEVEL_REWARDS)
            .where(LEVEL_REWARDS.SERVER_ID.eq(guildId))
            .fetch();

        for (LevelRewardsRecord reward : rewards) {
            int rewardLevel = reward.getLevel();
            if (newLevel == rewardLevel) {
                Member member = guild.getMemberById(userId);
                if (reward.getType().equals("role")) {
                    Role rewardRole = guild.getRoleById(reward.getReward());
                    if (rewardRole != null && member != null && !member.getRoles().contains(rewardRole)) {
                        guild.addRoleToMember(member, rewardRole).queue();
                    }
                }
                if (reward.getType().equals("channel")) {
                    assert member != null;
                    Objects.requireNonNull(guild.getGuildChannelById(reward.getReward()))
                        .getPermissionContainer().upsertPermissionOverride(member)
                        .grant(Permission.VIEW_CHANNEL)
                        .queue();
                }
            }
        }
    }

    private void addUserToLeveling(DSLContext dslContext, String userId, String guildId) {
        dslContext
            .insertInto(USER_XP, USER_XP.SERVER_ID, USER_XP.USER_ID, USER_XP.LEVEL_XP, USER_XP.TOTAL_XP, USER_XP.LEVEL)
            .values(guildId, userId, 1, 1, 0)
            .execute();
    }
}
