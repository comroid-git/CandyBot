package org.comroid.candybot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.comroid.candybot.bank.BankVault;
import org.comroid.candybot.bank.CandyBank;
import org.comroid.common.io.FileHandle;
import org.comroid.crystalshard.DiscordAPI;
import org.comroid.crystalshard.DiscordBotBase;
import org.comroid.crystalshard.entity.guild.Guild;
import org.comroid.crystalshard.gateway.GatewayIntent;
import org.comroid.crystalshard.gateway.event.dispatch.guild.GuildCreateEvent;
import org.comroid.crystalshard.gateway.event.dispatch.message.MessageCreateEvent;
import org.comroid.crystalshard.ui.CommandDefinition;
import org.comroid.crystalshard.ui.CommandSetup;
import org.comroid.crystalshard.ui.InteractionCore;
import org.comroid.restless.adapter.okhttp.v4.OkHttp4Adapter;
import org.comroid.uniform.adapter.json.fastjson.FastJSONLib;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class CandyBot extends DiscordBotBase {
    public static final ThreadGroup GROUP = new ThreadGroup("candybot");
    public static final FileHandle DIR_DATA;
    public static final FileHandle DIR_LOGIN;
    public static final CandyBank CANDY_BANK;
    public static final DiscordAPI API;
    private static final Logger logger = LogManager.getLogger();
    public static CandyBot instance;

    static {
        DIR_DATA = new FileHandle("/srv/dcb/candybot/", true);
        DIR_LOGIN = DIR_DATA.createSubDir("login");
        DiscordAPI.SERIALIZATION = FastJSONLib.fastJsonLib;
        //ScheduledExecutorService pool = new MonitoredThreadPool(GROUP, logger, 8, 10, 30);
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(8);
        API = new DiscordAPI(new OkHttp4Adapter(), pool);
        CANDY_BANK = new CandyBank(DIR_DATA.createSubDir("vaults"));
    }

    private CandyBot(String token) {
        super(API, token, GatewayIntent.ALL_UNPRIVILEGED);

        getEventPipeline()
                .flatMap(GuildCreateEvent.class)
                .map(GuildCreateEvent::getGuild)
                .peek(guild -> System.out.printf("guild created: %s - id: %d\n", guild.getName(), guild.getID()));

        final InteractionCore core = getInteractionCore();
        CommandSetup config = core.getCommands();
        config.readClass(DiscordCommands.class);

        CommandDefinition stats = Objects.requireNonNull(config.getCommand("stats")),
                candy = Objects.requireNonNull(config.getCommand("candy")),
                dev = Objects.requireNonNull(config.getCommand("dev"));
        core.addCommandsToGuild(736946463661359155L, stats, candy, dev)
                .thenCompose(nil -> core.synchronizeGlobal())
                .join();

        getEventPipeline().flatMap(MessageCreateEvent.class)
                .flatMap(event -> event.message)
                .filter(message -> message.guild.isNonNull())
                .filter(message -> message.author.isNonNull())
                .peek(message -> {
                    Guild guild = message.getGuild();
                    BankVault vault = CANDY_BANK.getVault(guild);
                    if (!vault.countUp())
                        return;
                    vault.winner(message.getUserAuthor());
                    message.sendText(vault.getEmoji()).join();
                });
    }

    public static void main(String[] args) {
        instance = new CandyBot(DIR_LOGIN.createSubFile("discord.cred").getContent());
    }

    public static void shutdown() {
        logger.info("Shutting down!");
        try {
            CANDY_BANK.close();
            API.close();
        } catch (Throwable t) {
            logger.error("Error while shutting down", t);
        }
        System.exit(0);
    }
}
