package org.shithackers.commands.other;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;

import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PollCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getFullCommandName().equals("poll create")) {
            String username = "postgres";
            String password = "root";
            String url = "jdbc:postgresql://localhost:5432/ShitBot_db";

            Guild guild = event.getGuild();

            String pollMessage = Objects.requireNonNull(event.getOption("message")).getAsString();

            List<String> emojis = Arrays.asList(
                "\u0031\u20E3", "\u0032\u20E3", "\u0033\u20E3", "\u0034\u20E3", "\u0035\u20E3",
                "\u0036\u20E3", "\u0037\u20E3", "\u0038\u20E3", "\u0039\u20E3", "\uD83D\uDD1F");

            try {
                Connection connection = DriverManager.getConnection(url, username, password);
                PreparedStatement st = connection.prepareStatement(
                    "SELECT server_id, message, options FROM polls WHERE server_id = ?");
                assert guild != null;
                st.setString(1, guild.getId());
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    if (rs.getString("server_id").equals(guild.getId())) {
                        if (rs.getString("message").equals(pollMessage)) {
                            event.reply("Poll already exists").setEphemeral(true).queue();
                            return;
                        }
                    }
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
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

            try {
                Connection connection = DriverManager.getConnection(url, username, password);

                PreparedStatement st = connection.prepareStatement(
                    "INSERT INTO polls (server_id, poll_id, message, options, reactions) VALUES (?, ?, ?, ?, ?)");

                st.setString(1, guild.getId());
                st.setString(2, message.getId());
                st.setString(3, pollMessage);
                st.setArray(4, connection.createArrayOf("varchar", optionsString.toArray()));
                st.setArray(5, connection.createArrayOf("int", reactions.toArray()));
                st.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (event.getFullCommandName().equals("poll show")) {
            String username = "postgres";
            String password = "root";
            String url = "jdbc:postgresql://localhost:5432/ShitBot_db";

            Guild guild = event.getGuild();

            try {
                Connection connection = DriverManager.getConnection(url, username, password);
                PreparedStatement st = connection.prepareStatement(
                    "SELECT server_id, poll_id FROM polls WHERE poll_id = ?");
                st.setString(1, Objects.requireNonNull(event.getOption("poll")).getAsString());
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    assert guild != null;
                    if (rs.getString("server_id").equals(guild.getId())) {
                        if (rs.getString("poll_id").equals(event.getOption("poll").getAsString())) {
                            st = connection.prepareStatement(
                                "SELECT cardinality(options), message, options, reactions FROM polls WHERE poll_id = ?");
                            st.setString(1, event.getOption("poll").getAsString());
                            rs = st.executeQuery();
                            if (rs.next()) {
                                List<String> optionValues = Arrays.asList((String[]) rs.getArray("options").getArray());
                                List<Integer> reactionValues = Arrays.asList((Integer[]) rs.getArray("reactions").getArray());
                                EmbedBuilder builder = new EmbedBuilder()
                                    .setTitle(rs.getString("message"))
                                    .setColor(Color.GREEN);
                                for (int i = 0; i < rs.getInt(1); i++) {
                                    builder.addField(optionValues.get(i), reactionValues.get(i).toString(), false);
                                }
                                event.replyEmbeds(builder.build()).queue();
                            }
                        } else {
                            event.reply("Poll doesn't exist").setEphemeral(true).queue();
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (event.getFullCommandName().equals("poll delete")) {
            String username = "postgres";
            String password = "root";
            String url = "jdbc:postgresql://localhost:5432/ShitBot_db";

            String pollId = event.getOption("poll").getAsString();

            RestAction<Message> retrieveAction = event.getChannel().retrieveMessageById(pollId);

            retrieveAction.submit().whenComplete((message, throwable) -> {
                if (throwable == null) {
                    try {
                        Connection connection = DriverManager.getConnection(url, username, password);
                        PreparedStatement st = connection.prepareStatement(
                            "select message from polls where poll_id = ?");
                        st.setString(1, pollId);
                        ResultSet rs = st.executeQuery();
                        if (rs.next()) {
                            event.reply("Poll \"" + rs.getString("message") + "\" deleted").setEphemeral(true).queue();
                        }
                        st = connection.prepareStatement(
                            "DELETE FROM polls WHERE poll_id = ?");
                        st.setString(1, pollId);
                        st.executeUpdate();
                        message.delete().queue();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
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
        if (Objects.requireNonNull(event.getUser()).isBot()) return;
        char emojiName = event.getReaction().getEmoji().getName().charAt(0);

        String username = "postgres";
        String password = "root";
        String url = "jdbc:postgresql://localhost:5432/ShitBot_db";

        try {
            Connection connection = DriverManager.getConnection(url, username, password);

            PreparedStatement st = connection.prepareStatement(
                "SELECT reactions FROM polls WHERE poll_id = ?");
            st.setString(1, event.getMessageId());

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                Array array = rs.getArray("reactions");
                Integer[] reactions = (Integer[]) array.getArray();
                int a = emojiName - '1';
                reactions[a]++;
                st = connection.prepareStatement(
                    "UPDATE polls SET reactions = ? WHERE poll_id = ?");
                st.setArray(1, connection.createArrayOf("int", reactions));
                st.setString(2, event.getMessageId());
                st.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        if (Objects.requireNonNull(event.getUser()).isBot()) return;
        char emojiName = event.getReaction().getEmoji().getName().charAt(0);

        String username = "postgres";
        String password = "root";
        String url = "jdbc:postgresql://localhost:5432/ShitBot_db";

        try {
            Connection connection = DriverManager.getConnection(url, username, password);

            PreparedStatement st = connection.prepareStatement(
                "SELECT reactions FROM polls WHERE poll_id = ?");
            st.setString(1, event.getMessageId());

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                Array array = rs.getArray("reactions");
                Integer[] reactions = (Integer[]) array.getArray();
                int a = emojiName - '1';
                reactions[a]--;
                st = connection.prepareStatement(
                    "UPDATE polls SET reactions = ? WHERE poll_id = ?");
                st.setArray(1, connection.createArrayOf("int", reactions));
                st.setString(2, event.getMessageId());
                st.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
