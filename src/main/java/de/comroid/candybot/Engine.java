package de.comroid.candybot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.comroid.CandyBot;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.util.logging.ExceptionLogger;

public enum Engine implements MessageCreateListener {
    INSTANCE;

    private final Map<Server, Integer> counters = new ConcurrentHashMap<>();

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

        if (counters.get(server) >= CandyBot.PROP.getProperty("candy.limit").getValue(server.getId()).asInt()) {
            counters.computeIfPresent(server, (k, v) -> 0);

            CandyBank.INSTANCE.increment(server, user);

            event.getChannel()
                    .sendMessage(CandyBot.PROP
                            .getProperty("candy.emoji")
                            .getValue(server.getId())
                            .asString())
                    .exceptionally(ExceptionLogger.get());
        }
    }
}
