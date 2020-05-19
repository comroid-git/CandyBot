package org.comroid;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.rest.util.Color;
import discord4j.rest.util.Snowflake;
import org.comroid.candybot.GuildConfiguration;
import org.comroid.candybot.GuildConfigurationBuilder;
import org.comroid.common.map.TrieStringMap;
import org.comroid.util.files.FileProvider;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public final class CandyBot {
    public static final CandyBot instance;
    public static final String DEFAULT_EMOJI = "\uD83C\uDF61"; //dango
    public static final Color THEME_COLOR = new Color(0xcf2f2f);
    private static final String INVITE = "https://discord.com/oauth2/authorize?client_id=487745829617139722&scope=bot&permissions=84992";

    static {
        try {
            final String token = new BufferedReader(new FileReader(FileProvider
                    .getFile("login/discord.cred"))).readLine();

            instance = new CandyBot(token);
        } catch (IOException e) {
            throw new RuntimeException("Could not read file", e);
        }
    }

    public final GatewayDiscordClient client;
    private final Map<Guild, GuildConfiguration> configs;

    public CandyBot(String token) {
        client = Objects.requireNonNull(DiscordClient.create(token).login().block(), "Client could not be initialized");
        configs = new TrieStringMap<>(
                guild -> guild.getId().asString(),
                id -> client.getGuildById(Snowflake.of(id)).block()
        );

        client.on(MessageCreateEvent.class)
                .subscribe(this::handleMessageCreate);
    }

    public static void main(final String[] args) throws InterruptedException {
        System.out.println("CandyBot started");
    }

    private void handleMessageCreate(MessageCreateEvent event) {
        if (!event.getMessage()
                .getChannel()
                .as(GuildMessageChannel.class::isInstance)
                || event.getMember().isPresent()) {
            Objects.requireNonNull(event.getMessage()
                    .getChannel()
                    .block())
                    .createMessage(INVITE);
            return;
        }

        final GuildConfiguration configuration = compute(event.getGuild().block());

        if (configuration.getCounter().updateAndGet(x -> x + 1) >= configuration.getLimit()) {
            concludeCycle(
                    configuration,
                    event.getMessage().getChannel().as(GuildMessageChannel.class::cast),
                    event.getMember().get()
            );
        }
    }

    private void concludeCycle(GuildConfiguration configuration, GuildMessageChannel channel, Member user) {
        channel.createMessage(configuration.getEmoji()).block();

        configuration.getCounter().set(0);
    }

    private GuildConfiguration compute(Guild forGuild) {
        return configs.computeIfAbsent(forGuild, nil -> new GuildConfigurationBuilder(this)
                .setGuild(forGuild)
                .build());
    }
}
