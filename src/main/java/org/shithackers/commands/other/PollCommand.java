package org.shithackers.commands.other;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record3;
import org.shithackers.db.tables.records.PollsRecord;
import org.shithackers.utils.ShitBotDatabase;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.shithackers.db.tables.Polls.POLLS;

public class PollCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getFullCommandName().equals("poll create")) {
            Guild guild = event.getGuild();
            assert guild != null;

            String pollMessage = Objects.requireNonNull(event.getOption("message")).getAsString();

            List<String> emojis = Arrays.asList(
                "\u0031\u20E3", "\u0032\u20E3", "\u0033\u20E3", "\u0034\u20E3", "\u0035\u20E3",
                "\u0036\u20E3", "\u0037\u20E3", "\u0038\u20E3", "\u0039\u20E3", "\uD83D\uDD1F");

            DSLContext dslContext = ShitBotDatabase.getDSLContext();

            PollsRecord poll = dslContext.selectFrom(POLLS)
                .where(POLLS.SERVER_ID.eq(guild.getId()))
                .fetchOne();

            if (poll != null) {
                if (poll.getMessage().equals(pollMessage)) {
                    event.reply("Poll already exists").setEphemeral(true).queue();
                    return;
                }
            }

            List<OptionMapping> optionMappings = event.getOptions()
                .stream()
                .filter(option -> !option.getName().equals("message"))
                .collect(Collectors.toList());

            List<String> options = optionMappings.stream()
                .map(option -> emojis.get(optionMappings.indexOf(option)) + " " + option.getAsString())
                .collect(Collectors.toList());

            event.replyEmbeds(
                new EmbedBuilder()
                    .setDescription("Poll created")
                    .setColor(Color.GREEN)
                    .build()
            ).setEphemeral(true).queue();

            Message message = event.getChannel().sendMessageEmbeds(
                new EmbedBuilder()
                    .setTitle(pollMessage)
                    .setDescription(String.join("\n\n", options))
                    .build()
            ).complete();
            for (int i = 0; i < options.size(); i++) {
                message.addReaction(Emoji.fromUnicode(emojis.get(i))).queue();
            }

            /*ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.schedule(() -> {
                message.delete().queue();
                scheduler.shutdown();
            }, 1, TimeUnit.HOURS);*/

            List<String> optionsString = new ArrayList<>();
            for (OptionMapping option : optionMappings) {
                optionsString.add(option.getAsString());
            }

            List<Integer> reactions = new ArrayList<>();
            for (int i = 0; i < options.size(); i++) {
                reactions.add(0);
            }

            dslContext.insertInto(POLLS)
                .set(POLLS.SERVER_ID, guild.getId())
                .set(POLLS.POLL_ID, message.getId())
                .set(POLLS.MESSAGE, pollMessage)
                .set(POLLS.OPTIONS, optionsString.toArray(new String[0]))
                .set(POLLS.REACTIONS, reactions.toArray(new Integer[0]))
                .execute();
        }

        if (event.getFullCommandName().equals("poll show")) {
            Guild guild = event.getGuild();

            DSLContext dslContext = ShitBotDatabase.getDSLContext();

            Record1<String> serverId = dslContext.select(POLLS.SERVER_ID)
                .from(POLLS)
                .where(POLLS.POLL_ID.eq(Objects.requireNonNull(event.getOption("poll")).getAsString()))
                .fetchOne();
            if (serverId != null) {
                assert guild != null;
                if (serverId.value1().equals(guild.getId())) {
                    Record3<String, String[], Integer[]> poll = dslContext.select(POLLS.MESSAGE, POLLS.OPTIONS, POLLS.REACTIONS)
                        .from(POLLS)
                        .where(POLLS.POLL_ID.eq(Objects.requireNonNull(event.getOption("poll")).getAsString()))
                        .fetchOne();

                    if (poll != null) {
                        List<String> optionValues = Arrays.asList(poll.get(POLLS.OPTIONS));
                        List<Integer> reactionValues = Arrays.asList(poll.get(POLLS.REACTIONS));
                        EmbedBuilder builder = new EmbedBuilder()
                            .setTitle(poll.get(POLLS.MESSAGE))
                            .setColor(Color.GREEN);

                        for (int i = 0; i < optionValues.size(); i++) {
                            builder.addField(optionValues.get(i), reactionValues.get(i).toString(), false);
                        }

                        event.replyEmbeds(builder.build()).queue();
                    } else {
                        event.reply("Poll doesn't exist").setEphemeral(true).queue();
                    }
                }
            }
        }

        if (event.getFullCommandName().equals("poll delete")) {
            String pollId = Objects.requireNonNull(event.getOption("poll")).getAsString();
            RestAction<Message> retrieveAction = event.getChannel().retrieveMessageById(pollId);

            retrieveAction.submit().whenComplete((message, throwable) -> {
                if (throwable == null) {
                    DSLContext dslContext = ShitBotDatabase.getDSLContext();

                    Record1<String> pollMessage = dslContext.select(POLLS.MESSAGE)
                        .from(POLLS)
                        .where(POLLS.POLL_ID.eq(pollId))
                        .fetchOne();

                    if (pollMessage != null) {
                        event.reply("Poll \"" + pollMessage.value1() + "\" deleted").setEphemeral(true).queue();
                    }
                    dslContext.deleteFrom(POLLS)
                        .where(POLLS.POLL_ID.eq(pollId))
                        .execute();

                    message.delete().queue();
                } else if (throwable instanceof ErrorResponseException) {
                    ErrorResponseException error = (ErrorResponseException) throwable;

                    if (error.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) {
                        event.reply("Poll doesn't exist").setEphemeral(true).queue();
                    }
                } else {
                    throwable.printStackTrace();
                }
            });
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (Objects.requireNonNull(event.getUser()).isBot()) {
            return;
        }

        char emojiName = event.getReaction().getEmoji().getName().charAt(0);
        updatePollReactions(event.getMessageId(), emojiName, 1);
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        if (Objects.requireNonNull(event.getUser()).isBot()) {
            return;
        }

        char emojiName = event.getReaction().getEmoji().getName().charAt(0);
        updatePollReactions(event.getMessageId(), emojiName, -1);
    }

    private void updatePollReactions(String messageId, char emojiName, int delta) {
        DSLContext dslContext = ShitBotDatabase.getDSLContext();

        Record1<Integer[]> reactionsRecord = dslContext.select(POLLS.REACTIONS)
            .from(POLLS)
            .where(POLLS.POLL_ID.eq(messageId))
            .fetchOne();

        if (reactionsRecord != null) {
            Integer[] reactions = reactionsRecord.value1();
            int index = emojiName - '1';
            reactions[index] += delta;
            dslContext.update(POLLS)
                .set(POLLS.REACTIONS, reactions)
                .where(POLLS.POLL_ID.eq(messageId))
                .execute();
        }
    }
}
