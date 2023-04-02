package org.shithackers.utils;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.shithackers.commands.info.ServerInfoCommand;
import org.shithackers.commands.info.UserInfoCommand;
import org.shithackers.commands.level.LevelCommand;
import org.shithackers.commands.level.LevelSystem;
import org.shithackers.commands.level.RewardCommand;
import org.shithackers.commands.mod.BanUserCommand;
import org.shithackers.commands.mod.MuteUserCommand;
import org.shithackers.commands.mod.ReportCommand;
import org.shithackers.commands.mod.WarnCommand;
import org.shithackers.commands.other.*;
import org.shithackers.listeners.LinksListener;
import org.shithackers.listeners.MessagesListener;

import java.util.ArrayList;
import java.util.List;

public class RegisterCommands {
    public static void register(JDA api) {
        api.addEventListener(
            new UserInfoCommand(),
            new ServerInfoCommand(),
            new BanUserCommand(),
            new MuteUserCommand(),
            new WarnCommand(),
            new WelcomeCommand(),
            new ReportCommand(),
            new WeatherCommand(),
            new MessagesListener(),
            new PrintQuoteCommand(),
            new AvatarCommand(),
            //new MusicCommand(),
            //new MemberMusicListener(),
            new TestCommand(),
            new LinksListener(),
            new VerifyCommand(),
            new PollCommand(),
            new LevelSystem(),
            new LevelCommand(),
            new RewardCommand()
        );

        OptionData reward = new OptionData(OptionType.STRING, "reward", "Reward", true)
            .addChoice("add role", "role")
            .addChoice("get access to channel", "channel");

        List<OptionData> choices = new ArrayList<>();
        choices.add(new OptionData(OptionType.STRING, "message", "message", true));
        for (int i = 1; i <= 10; i++) {
            boolean isRequired = i <= 2;
            choices.add(new OptionData(OptionType.STRING, "choice" + i, "choice" + i, isRequired));
        }

        api.updateCommands().addCommands(
            Commands.slash("test", "test"),

            Commands.slash("level", "Get your level")
                .addSubcommands(
                    new SubcommandData("show", "Show your level"),
                    new SubcommandData("leaderboard", "Show the level leaderboard"),
                    new SubcommandData("reward", "Set a reward for a level")
                        .addOption(OptionType.STRING, "level", "Level", true)
                        .addOptions(reward),
                    new SubcommandData("rewards", "Get list of rewards")
                ),

            Commands.slash("poll", "Create a poll")
                .addSubcommands(
                    new SubcommandData("create", "Create a poll")
                        .addOptions(choices),
                    new SubcommandData("show", "Show a poll")
                        .addOption(OptionType.STRING, "poll", "Poll ID", true),
                    new SubcommandData("delete", "Show a poll")
                        .addOption(OptionType.STRING, "poll", "Poll ID", true)
                ),

            Commands.slash("play", "Play a song")
                .addOption(OptionType.STRING, "link", "Insert youtube link here", false),
            Commands.slash("skip", "Skip the current song"),
            Commands.slash("stop", "Stop the music"),

            Commands.slash("avatar", "Get the avatar of a user")
                .addOption(OptionType.USER, "user", "The user to get the avatar of", false),

            Commands.slash("user", "Get info about a user")
                .addOption(OptionType.USER, "user", "The user to get info about", true),

            Commands.slash("server", "Get info about the server"),

            Commands.slash("ban", "Ban a user")
                .addOption(OptionType.USER, "user", "The user to ban", true)
                .addOption(OptionType.INTEGER, "days", "The amount of days to delete messages for", false)
                .addOption(OptionType.STRING, "reason", "The reason for the ban", false),
            Commands.slash("unban", "Unban a user")
                .addOption(OptionType.USER, "user", "The user to unban", true)
                .addOption(OptionType.STRING, "reason", "The reason for the unban", false),

            Commands.slash("mute", "Mute a user")
                .addOption(OptionType.USER, "user", "The user to mute", true)
                .addOption(OptionType.STRING, "duration", "The duration of the mute\nFor example: 5s, 15m, 1h", true)
                .addOption(OptionType.STRING, "reason", "The reason for the mute", false),
            Commands.slash("unmute", "Unmute a user")
                .addOption(OptionType.USER, "user", "The user to unmute", true)
                .addOption(OptionType.STRING, "reason", "The reason for the unmute", false),

            Commands.slash("report", "Report something")
                .addOption(OptionType.STRING, "reason", "reason", true),

            Commands.slash("warn", "Warn a user")
                .addSubcommands(
                    new SubcommandData("user", "The member")
                        .addOption(OptionType.USER, "user", "Warn a member", true)
                        .addOption(OptionType.STRING, "reason", "The reason for the warn", false),
                    new SubcommandData("delete", "Delete a warn for user")
                        .addOption(OptionType.USER, "user", "The member", true),
                    new SubcommandData("clean", "Clear all warns for a user")
                        .addOption(OptionType.USER, "user", "The member", true)
                        .addOption(OptionType.STRING, "reason", "The reason to clear", false),
                    new SubcommandData("list", "Get the warns for all members")
                ),
            Commands.slash("warns", "Get the warns of a user")
                .addOption(OptionType.USER, "user", "The member", true),

            Commands.slash("channel", "create or set the channel")
                .addSubcommandGroups(new SubcommandGroupData("set", "set the channel")
                    .addSubcommands(
                        new SubcommandData("welcome", "welcome channel")
                            .addOption(OptionType.CHANNEL, "channel", "The channel to set as the welcome channel", true),
                        new SubcommandData("moderation", "moderation channel")
                            .addOption(OptionType.CHANNEL, "channel", "The channel to set as the welcome channel", true)
                    )
                )
                .addSubcommandGroups(new SubcommandGroupData("create", "create the channel")
                    .addSubcommands(new SubcommandData("verify", "The channel to create as verification channel"))
                )
                .addSubcommandGroups(new SubcommandGroupData("delete", "create the channel")
                    .addSubcommands(new SubcommandData("verify", "The channel to create as verification channel"))
                ),

            Commands.slash("weather", "Get the weather of a city")
                .addOption(OptionType.STRING, "city", "The city to get the weather of", true),

            Commands.slash("quote", "Get a random quote")
                .addSubcommands(
                    new SubcommandData("random", "Get a random quote"),
                    new SubcommandData("start", "Start the quotes")
                        .addOption(OptionType.CHANNEL, "channel", "The channel to start the quote in", true),
                    new SubcommandData("stop", "Stop the quotes")
                )
        ).queue();
    }
}
