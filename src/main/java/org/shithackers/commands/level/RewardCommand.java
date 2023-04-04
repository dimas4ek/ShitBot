package org.shithackers.commands.level;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Result;
import org.shithackers.db.Tables;
import org.shithackers.db.tables.LevelRewards;
import org.shithackers.db.tables.UserXp;
import org.shithackers.utils.ShitBotDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RewardCommand extends ListenerAdapter {
    String level;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getFullCommandName().equals("level reward create")) {
            level = Objects.requireNonNull(event.getOption("level")).getAsString();
            String reward = Objects.requireNonNull(event.getOption("reward")).getAsString();

            createSelectMenu(event, reward, "Create");
        }
        if (event.getFullCommandName().equals("level reward remove")) {
            String reward = Objects.requireNonNull(event.getOption("reward")).getAsString();

            createSelectMenu(event, reward, "Remove");
        }
        if (event.getFullCommandName().equals("level reward list")) {
            DSLContext dslContext = ShitBotDatabase.getDSLContext();

            getRewardList(event, dslContext);
        }
    }

    @Override
    public void onEntitySelectInteraction(EntitySelectInteractionEvent event) {
        String componentId = event.getComponentId();
        String type;
        String rewardId;

        if (componentId.equals("roleCreate") || componentId.equals("roleRemove")) {
            Role role = event.getMentions().getRoles().get(0);
            type = "role";
            rewardId = role.getId();
        } else if (componentId.equals("channelCreate") || componentId.equals("channelRemove")) {
            Channel channel = event.getMentions().getChannels().get(0);
            type = "channel";
            rewardId = channel.getId();
        } else {
            return;
        }

        if (componentId.equals("roleRemove") || componentId.equals("channelRemove")) {
            DSLContext dslContext = ShitBotDatabase.getDSLContext();

            removeReward(event, rewardId, dslContext);
        }
        if (componentId.equals("roleCreate") || componentId.equals("channelCreate")) {
            DSLContext dslContext = ShitBotDatabase.getDSLContext();

            createReward(event, type, rewardId, dslContext);
        }
    }

    private static void createSelectMenu(SlashCommandInteractionEvent event, String reward, String action) {
        EntitySelectMenu menu;
        if (reward.equals("role")) {
            menu = EntitySelectMenu.create("role" + action, EntitySelectMenu.SelectTarget.ROLE).build();
            event.reply("Choose role to add").addComponents(ActionRow.of(menu)).setEphemeral(true).queue();
        } else if (reward.equals("channel")) {
            menu = EntitySelectMenu.create("channel" + action, EntitySelectMenu.SelectTarget.CHANNEL).build();
            event.reply("Choose channel to add").addComponents(ActionRow.of(menu)).queue();
        }
    }

    private static void getRewardList(SlashCommandInteractionEvent event, DSLContext dslContext) {
        LevelRewards levelRewards = Tables.LEVEL_REWARDS;

        Result<Record3<Integer, String, String>> result =
            dslContext
                .select(
                    levelRewards.LEVEL,
                    levelRewards.REWARD,
                    levelRewards.TYPE)
                .from(levelRewards)
                .where(levelRewards.SERVER_ID.eq(Objects.requireNonNull(event.getGuild()).getId()))
                .fetch();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Rewards");

        List<String> levels = new ArrayList<>();
        List<String> rewards = new ArrayList<>();
        List<String> types = new ArrayList<>();

        for (Record3<Integer, String, String> record : result) {
            levels.add(String.valueOf(record.get(levelRewards.LEVEL)));

            String type = record.get(levelRewards.TYPE);

            if (type.equals("role")) {
                Role role = event.getGuild().getRoleById(record.get(levelRewards.REWARD));
                if (role != null) {
                    rewards.add(role.getAsMention());
                }
            }
            if (type.equals("channel")) {
                GuildChannel channel = event.getGuild().getGuildChannelById(record.get(levelRewards.REWARD));
                if (channel != null) {
                    rewards.add(channel.getAsMention());
                }
            }

            types.add(type);
        }

        builder.addField("Level", String.join("\n\n", levels), true);
        builder.addField("Reward", String.join("\n\n", rewards), true);
        builder.addField("Type", String.join("\n\n", types), true);

        event.replyEmbeds(builder.build()).queue();
    }

    private void createReward(EntitySelectInteractionEvent event, String type, String rewardId, DSLContext dslContext) {
        LevelRewards levelRewards = LevelRewards.LEVEL_REWARDS;

        boolean rewardExists = dslContext.selectFrom(levelRewards)
            .where(levelRewards.SERVER_ID.eq(event.getGuild().getId())
                .and(levelRewards.REWARD.eq(rewardId)))
            .fetchOne() != null;

        if (rewardExists) {
            dslContext.update(levelRewards)
                .set(levelRewards.LEVEL, Integer.parseInt(level))
                .where(levelRewards.SERVER_ID.eq(event.getGuild().getId())
                    .and(levelRewards.REWARD.eq(rewardId)))
                .execute();
        } else {
            dslContext.insertInto(levelRewards)
                .set(levelRewards.SERVER_ID, event.getGuild().getId())
                .set(levelRewards.LEVEL, Integer.parseInt(level))
                .set(levelRewards.REWARD, rewardId)
                .set(levelRewards.TYPE, type)
                .execute();
        }

        Result<Record3<Integer, String, String>> rewards =
            dslContext
                .select(
                    levelRewards.LEVEL,
                    levelRewards.REWARD,
                    levelRewards.TYPE)
                .from(levelRewards)
                .where(levelRewards.SERVER_ID.eq(event.getGuild().getId())
                    .and(levelRewards.TYPE.eq(type)))
                .fetch();

        List<Member> members = event.getGuild().getMembers();
        for (Record3<Integer, String, String> reward : rewards) {
            int rewardLevel = reward.get(levelRewards.LEVEL);

            for (Member member : members) {
                Result<Record1<Integer>> userLevels = dslContext.select(UserXp.USER_XP.LEVEL)
                    .from(UserXp.USER_XP)
                    .where(UserXp.USER_XP.SERVER_ID.eq(event.getGuild().getId())
                        .and(UserXp.USER_XP.USER_ID.eq(member.getId())))
                    .fetch();

                for (Record1<Integer> userLevel : userLevels) {
                    int userXpLevel = userLevel.get(UserXp.USER_XP.LEVEL);

                    if (userXpLevel < rewardLevel) {
                        if (type.equals("role")) {
                            String roleId = reward.get(levelRewards.REWARD);
                            Role role = event.getGuild().getRoleById(roleId);
                            assert role != null;
                            event.getGuild().removeRoleFromMember(member, role).queue();
                        }
                        if (type.equals("channel")) {
                            Objects.requireNonNull(event.getGuild().getGuildChannelById(reward.get(levelRewards.REWARD)))
                                .getPermissionContainer().upsertPermissionOverride(member)
                                .clear(Permission.VIEW_CHANNEL).queue();
                        }
                    }
                    if (userXpLevel >= rewardLevel) {
                        if (type.equals("role")) {
                            Role rewardRole = event.getGuild().getRoleById(rewardId);
                            if (rewardRole != null && !member.getRoles().contains(rewardRole)) {
                                event.getGuild().addRoleToMember(member, rewardRole).queue();
                            }
                        }
                        if (type.equals("channel")) {
                            Objects.requireNonNull(event.getGuild().getGuildChannelById(rewardId))
                                .getPermissionContainer().upsertPermissionOverride(member)
                                .grant(Permission.VIEW_CHANNEL).queue();
                        }
                    }
                }
            }
        }

        String rewardMention = null;
        if (type.equals("role")) {
            rewardMention = event.getMentions().getRoles().get(0).getAsMention();
        }
        if (type.equals("channel")) {
            rewardMention = event.getMentions().getChannels().get(0).getAsMention();
        }

        event.deferEdit().delay(3, TimeUnit.SECONDS).flatMap(InteractionHook::deleteOriginal).queue();
        //event.reply("Reward " + rewardMention + " has been successfully added to level " + level).setEphemeral(true).queue();
    }

    private static void removeReward(EntitySelectInteractionEvent event, String rewardId, DSLContext dslContext) {
        LevelRewards levelRewards = LevelRewards.LEVEL_REWARDS;
        dslContext.deleteFrom(levelRewards)
            .where(levelRewards.SERVER_ID.eq(Objects.requireNonNull(event.getGuild()).getId()))
            .and(levelRewards.REWARD.eq(rewardId))
            .execute();

        event.deferEdit().delay(3, TimeUnit.SECONDS).flatMap(InteractionHook::deleteOriginal).queue();
    }
}
