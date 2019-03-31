package de.kaleidox.dangobot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.kaleidox.DangoBot;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.util.logging.ExceptionLogger;

public enum Engine implements MessageCreateListener {
    INSTANCE;

    private Map<Server, Integer> counters = new ConcurrentHashMap<>();

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.isPrivateMessage()) return;

        Server server = event.getServer().orElseThrow(AssertionError::new);
        User user = event.getMessageAuthor().asUser().orElse(null);

        if (user == null) return;

        if (event.getMessageAuthor().asUser().map(User::isBot).map(bool -> !bool).orElse(false))
            counters.compute(server, (subServer, counter) -> {
                if (counter == null) counter = 0;
                return counter + 1;
            });

        if (counters.get(server) >= DangoBot.PROP.getProperty("dango.limit").getValue(server.getId()).asInt()) {
            counters.computeIfPresent(server, (k, v) -> 0);

            DangoBank.INSTANCE.increment(server, user);

            event.getChannel()
                    .sendMessage(DangoBot.PROP
                            .getProperty("dango.emoji")
                            .getValue(server.getId())
                            .asString())
                    .exceptionally(ExceptionLogger.get());
        }
    }
}
