package org.shithackers.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MessagesListener extends ListenerAdapter {

    List<String> messageToDelete = new ArrayList<String>() {{
        add("hui");
        add("chlen");
        add("zopa");
    }};

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        for (String message : messageToDelete) {
            if (event.getMessage().getContentRaw().equals(message)) {
                String messageId = event.getChannel().getLatestMessageId();
                event.getChannel().deleteMessageById(messageId).queue();
            }
        }
    }
}
