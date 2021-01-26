package org.comroid.candybot;

import org.comroid.candybot.bank.BankVault;
import org.comroid.candybot.bank.CandyBank;
import org.comroid.commandline.CommandLineArgs;
import org.comroid.common.io.FileHandle;
import org.comroid.crystalshard.DiscordAPI;
import org.comroid.crystalshard.DiscordBotBase;
import org.comroid.crystalshard.entity.guild.Guild;
import org.comroid.crystalshard.entity.user.User;
import org.comroid.crystalshard.gateway.GatewayIntent;
import org.comroid.crystalshard.gateway.event.dispatch.message.MessageCreateEvent;
import org.comroid.crystalshard.ui.CommandDefinition;
import org.comroid.crystalshard.ui.CommandSetup;
import org.comroid.crystalshard.ui.InteractionCore;
import org.comroid.restless.adapter.okhttp.v4.OkHttp4Adapter;
import org.comroid.uniform.adapter.json.fastjson.FastJSONLib;

import java.util.Objects;

public final class CandyBot extends DiscordBotBase {
    public static final FileHandle DIR_DATA;
    public static final FileHandle DIR_LOGIN;
    public static final CandyBank CANDY_BANK;
    public static final DiscordAPI API;
    public static CommandLineArgs ARGS;
    public static CandyBot instance;

    static {
        DIR_DATA = new FileHandle("/srv/dcb/candybot/", true);
        DIR_LOGIN = DIR_DATA.createSubDir("login");
        DiscordAPI.SERIALIZATION = FastJSONLib.fastJsonLib;
        API = new DiscordAPI(new OkHttp4Adapter());
        CANDY_BANK = new CandyBank(DIR_DATA.createSubDir("vaults"));
    }

    private CandyBot(String token) {
        super(API, token, GatewayIntent.ALL_UNPRIVILEGED);

        InteractionCore core = getInteractionCore();
        CommandSetup commands = core.getCommands();
        commands.readClass(DiscordCommands.class);
        core.synchronizeGlobal().join();

        CommandDefinition stats = Objects.requireNonNull(commands.getCommand("stats"));
        commands.addGuildDefinition(736946463661359155L, stats);
        CommandDefinition candy = Objects.requireNonNull(commands.getCommand("candy"));
        commands.addGuildDefinition(736946463661359155L, candy);
        CommandDefinition dev = Objects.requireNonNull(commands.getCommand("dev"));
        commands.addGuildDefinition(736946463661359155L, dev);

        core.synchronizeGuild(736946463661359155L).join();

        getEventPipeline().flatMap(MessageCreateEvent.class)
                .flatMap(event -> event.message)
                .filter(message -> message.guild.isNonNull())
                .filter(message -> message.author.isNonNull())
                .forEach(message -> {
                    Guild guild = message.getGuild();
                    BankVault vault = CANDY_BANK.getVault(guild);
                    if (!vault.countUp())
                        return;
                    vault.winner(message.getUserAuthor());
                    message.sendText(vault.getEmoji()).join();
                });
    }

    public static void main(String[] args) {
        ARGS = CommandLineArgs.parse(args);

        Runtime.getRuntime().addShutdownHook(new Thread(CandyBot::shutdown));

        instance = new CandyBot(ARGS.wrap("token").orElseGet(DIR_LOGIN.createSubFile("discord.cred")::getContent));
    }

    public static void shutdown() {
        try {
            instance.close();
            CANDY_BANK.close();
        } catch (Throwable t) {
            throw new RuntimeException("error while closing", t);
        }
    }
}
