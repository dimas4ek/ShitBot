package org.shithackers.utils;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.shithackers.commands.info.ServerInfoCommand;
import org.shithackers.commands.info.UserInfoCommand;
import org.shithackers.commands.mod.BanUserCommand;
import org.shithackers.commands.mod.MuteUserCommand;
import org.shithackers.commands.mod.ReportCommand;
import org.shithackers.commands.mod.WarnCommand;
import org.shithackers.commands.music.MusicCommand;
import org.shithackers.commands.other.*;
import org.shithackers.listeners.*;

public class RegisterCommands {
    public static void register(JDA api) {
        api.addEventListener(
            new UserInfoCommand(),
            new ServerInfoCommand(),
            new BanUserCommand(),
            new MuteUserCommand(),
            new WarnCommand(),
            new WelcomeCommand(),
            //new UserJoinLeaveListener(),
            new ReportCommand(),
            new WeatherCommand(),
            new MessagesListener(),
            new PrintQuoteCommand(),
            new AvatarCommand(),
            new MusicCommand(),
            new MemberMusicListener(),
            new TestCommand(),
            new LinksListener(),
            //new VerifyListener(),
            new VerifyCommand()
        );

        api.updateCommands().addCommands(
            Commands.slash("test", "test"),

            Commands.slash("play", "Play a song")
                .addOption(OptionType.STRING, "link", "Insert youtube link here", false),
            Commands.slash("skip", "Skip the current song"),
            Commands.slash("stop", "Stop the music"),

            Commands.slash("avatar", "Get the avatar of a user")
                .addOption(OptionType.USER, "user", "The user to get the avatar of", false),

            Commands.slash("userinfo", "Get info about a user")
                .addOption(OptionType.USER, "user", "The user to get info about", true),

            Commands.slash("serverinfo", "Get info about the server"),

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

            Commands.slash("warn", "Warn a user")
                .addSubcommands(
                    new SubcommandData("user", "The member")
                        .addOption(OptionType.USER, "user", "Warn a member", true)
                        .addOption(OptionType.STRING, "reason", "The reason for the warn", false),
                    new SubcommandData("delete", "Delete a warn for user")
                        .addOption(OptionType.USER, "user", "The member", true)
                        .addOption(OptionType.STRING, "reason", "The reason to delete a warn", false),
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

           /*Commands.slash("set-welcome-channel", "Set the welcome channel")
                .addOption(OptionType.CHANNEL, "channel", "The channel to set as the welcome channel", true),

            Commands.slash("set-moderation-channel", "Set the moderation channel")
                .addOption(OptionType.CHANNEL, "channel", "The channel to set as the moderation channel", true),
            Commands.slash("report", "Report a user")
                .addOption(OptionType.USER, "user", "The user to report", true)
                .addOption(OptionType.STRING, "reason", "The reason for the report", false),*/

            /*Commands.slash("create", "Set the verify channel")
                .addSubcommandGroups(new SubcommandGroupData("channel", "verify")
                    .addSubcommands(new SubcommandData("verify", "channel"))),*/

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
