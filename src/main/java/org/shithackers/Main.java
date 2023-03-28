package org.shithackers;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.shithackers.utils.RegisterCommands;

import java.util.EnumSet;

public class Main {
    private static final String TOKEN = "***";

    public static void main( String[] args ) {

        try {
            JDA api = JDABuilder.createDefault(TOKEN)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableCache(EnumSet.allOf(CacheFlag.class))
                .enableIntents(EnumSet.allOf(GatewayIntent.class))
                .build();

            RegisterCommands.register(api);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
