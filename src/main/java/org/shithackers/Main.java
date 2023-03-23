package org.shithackers;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.shithackers.utils.RegisterCommands;

public class Main {
    private static final String TOKEN = "***";

    public static void main( String[] args ) {

        try {
            JDA api = JDABuilder.createDefault(TOKEN)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_VOICE_STATES)
                .build();

            RegisterCommands.register(api);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
