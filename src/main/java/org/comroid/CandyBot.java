package org.comroid;

import com.google.common.flogger.FluentLogger;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.comroid.candybot.GuildConfiguration;
import org.comroid.candybot.GuildConfigurationBuilder;
import org.comroid.candybot.UserScore;
import org.comroid.common.Polyfill;
import org.comroid.dreadpool.ThreadPool;
import org.comroid.uniform.adapter.json.fastjson.FastJSONLib;
import org.comroid.uniform.cache.FileCache;
import org.comroid.util.files.FileProvider;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class CandyBot {
    public static final FluentLogger logger = FluentLogger.forEnclosingClass();
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

    public final ThreadPool threadPool;
    public final GatewayDiscordClient client;
    private final FileCache<Long, GuildConfiguration, CandyBot> configs;
    private final Object activity = Polyfill.selfawareLock();

    public CandyBot(String token) {
        this.threadPool = ThreadPool.fixedSize(new ThreadGroup("CandyBot"), 4);
        this.client = Objects.requireNonNull(DiscordClient.create(token)
                .login().block(), "Client could not be initialized");

        this.configs = new FileCache<>(
                FastJSONLib.fastJsonLib,
                GuildConfiguration.Bind.GuildId,
                FileProvider.getFile("data/guildConfigs.json"),
                250,
                this
        );

        threadPool.scheduleAtFixedRate(this::dataCycle, 5, 5, TimeUnit.MINUTES);

        client.on(GuildCreateEvent.class)
                .subscribe(event -> client.requestMembers(event.getGuild().getId()), this::handleThrowable);

        client.on(MessageCreateEvent.class)
                .subscribe(event -> {
                    synchronized (activity) {
                        handleMessageCreate(event);
                    }
                }, this::handleThrowable);
    }

    public static void main(final String[] args) {
        logger.at(Level.INFO).log("CANDYBOT STARTED");

        instance.client.onDisconnect().block();

        logger.at(Level.INFO).log("CANDYBOT STOPPING: DISCORD DISCONNECTED");
    }

    private void dataCycle() {
        synchronized (activity) {
            try {
                logger.at(Level.INFO).log("Saving Data...");
                configs.storeData();
            } catch (IOException e) {
                throw new RuntimeException("Could not store data", e);
            } finally {
                logger.at(Level.INFO).log("Data Saved!");
            }
        }
    }

    private void handleMessageCreate(MessageCreateEvent event) {
        if (event.getMember().map(User::isBot).orElse(false))
            return;

        final GuildMessageChannel guildMessageChannel = event.getMessage()
                .getChannel()
                .ofType(GuildMessageChannel.class)
                .block();
        if (guildMessageChannel == null) {
            Objects.requireNonNull(event.getMessage()
                    .getChannel()
                    .block())
                    .createMessage(INVITE)
                    .onErrorContinue(this::handleThrowable)
                    .subscribe();
            return;
        }

        final Guild guild = event.getGuild().block();
        if (guild == null)
            return;

        if (event.getMessage().getContent().startsWith("candy!")) {
            handleCommand(
                    guild,
                    event.getMessage().getChannel().block(),
                    event.getMessage(),
                    event.getMember().orElseThrow()
            );
            return;
        }

        final GuildConfiguration configuration = compute(guild);

        synchronized (activity) {
            if (configuration.getCounter()
                    .updateAndGet(x -> x + 1) >= configuration.getLimit()) {
                concludeCycle(
                        configuration,
                        guildMessageChannel,
                        event.getMember().get()
                );
            }
        }
    }

    public void handleThrowable(Throwable throwable) {
        logger.at(Level.SEVERE)
                .withCause(throwable)
                .log();
    }

    public void handleThrowable(Throwable throwable, Object o) {
        handleThrowable(throwable);
    }

    private void handleCommand(Guild guild, MessageChannel channel, Message command, Member user) {
        final GuildConfiguration configuration = compute(guild);

        switch (command.getContent()) {
            case "candy!own":
            case "candy!self":
                configuration.getScores()
                        .stream()
                        .filter(score -> score.getUser().equals(user))
                        .findFirst()
                        .ifPresentOrElse(
                                score -> channel.createEmbed(embed -> {
                                    personalizeEmbed(embed, user);

                                    embed.setDescription(String.format("You have %d points!", score.getScore()));
                                }).subscribe(),
                                () -> channel.createEmbed(embed -> {
                                    personalizeEmbed(embed, user);

                                    embed.setDescription("No scores were found for you. Sorry! :(");
                                }).subscribe()
                        );
                break;
            case "candy!stats":
                channel.createEmbed(embed -> {
                    personalizeEmbed(embed, user);

                    if (configuration.getScores().isEmpty()) {
                        embed.addField("No Scores set!", "Go ahead and chat a bit", false);
                        return;
                    }

                    final StringBuilder sb = new StringBuilder()
                            .append("__All Scores in ")
                            .append(guild.getName())
                            .append(":__");

                    final TreeMap<Integer, List<UserScore>> scores = new TreeMap<>(configuration.getScores()
                            .stream()
                            .collect(Collectors.groupingBy(UserScore::getScore)));

                    scores.forEach((count, users) -> {
                        sb.append("\n")
                                .append("**")
                                .append(count)
                                .append(" points:**");

                        users.stream()
                                .map(UserScore::getUser)
                                .map(User::getUsername)
                                .forEachOrdered(uname -> sb.append("\n")
                                        .append("\t- ")
                                        .append(uname));
                    });

                    if (sb.length() > 4000)
                        embed.addField("Too many scores!", "Currently, I can't display so many scores! I'm sorry.", false);

                    embed.setDescription(sb.toString());
                }).subscribe();

                break;
        }
    }

    private void personalizeEmbed(EmbedCreateSpec spec, Member member) {
        final Color userColor = member.getColor().block();

        if (userColor == null)
            spec.setColor(THEME_COLOR);
        else spec.setColor(userColor);

        spec.setFooter("Sent for " + member.getDisplayName(), member.getAvatarUrl());
    }

    private void concludeCycle(GuildConfiguration configuration, GuildMessageChannel channel, Member user) {
        synchronized (activity) {
            channel.createMessage(configuration.getEmoji()).block();

            final Optional<UserScore> any = configuration.getScores()
                    .stream()
                    .filter(score -> score.getUser().equals(user))
                    .findAny();

            if (any.isEmpty())
                configuration.initScoreboard(user);

            configuration.getCounter().set(0);
        }
    }

    private GuildConfiguration compute(Guild forGuild) {
        return configs.computeIfAbsent(forGuild.getId().asLong(),
                () -> new GuildConfigurationBuilder(this)
                        .setGuild(forGuild)
                        .build());
    }
}
