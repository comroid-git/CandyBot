package de.kaleidox;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import de.kaleidox.botstats.BotListSettings;
import de.kaleidox.botstats.javacord.JavacordStatsClient;
import de.kaleidox.botstats.model.StatsClient;
import de.kaleidox.candybot.CandyBank;
import de.kaleidox.candybot.Engine;
import de.kaleidox.candybot.command.AdminCommands;
import de.kaleidox.candybot.command.BasicCommands;
import de.kaleidox.candybot.command.CandyCommands;
import de.kaleidox.javacord.util.commands.CommandHandler;
import de.kaleidox.javacord.util.server.properties.ServerPropertiesManager;
import de.kaleidox.javacord.util.ui.embed.DefaultEmbedFactory;
import de.kaleidox.util.files.FileProvider;
import de.kaleidox.util.files.OSValidator;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.util.logging.ExceptionLogger;

public final class CandyBot {
    public final static Color THEME = new Color(0x34A6FF);

    public static final long BOT_ID = 487745829617139722L;

    public static final DiscordApi API;
    public static final CommandHandler CMD;
    public static final ServerPropertiesManager PROP;
    public static final StatsClient STAT;

    static {
        try {
            File file = FileProvider.getFile("login/token.cred");
            System.out.println("Looking for token file at " + file.getAbsolutePath());
            API = new DiscordApiBuilder()
                    .setToken(new BufferedReader(new FileReader(file)).readLine())
                    .login()
                    .exceptionally(ExceptionLogger.get())
                    .join();

            API.updateStatus(UserStatus.DO_NOT_DISTURB);
            API.updateActivity("Booting up...");

            STAT = new JavacordStatsClient(BotListSettings.builder()
                    .postStatsTester(OSValidator::isUnix)
                    .discordbotlist_com_token(new BufferedReader(new FileReader(FileProvider.getFile("login/token_dbl.cred"))).readLine())
                    .build(), API);

            DefaultEmbedFactory.setEmbedSupplier(() -> new EmbedBuilder().setColor(THEME));

            CMD = new CommandHandler(API);
            CMD.prefixes = new String[]{"candy!"};
            CMD.useDefaultHelp(null);
            CMD.registerCommands(BasicCommands.INSTANCE);
            CMD.registerCommands(CandyCommands.INSTANCE);
            CMD.registerCommands(AdminCommands.INSTANCE);

            PROP = new ServerPropertiesManager(FileProvider.getFile("data/serverProps.json"));
            PROP.usePropertyCommand(null, CMD);
            PROP.register("bot.customprefix", "!candy ")
                    .setDisplayName("Custom Prefix")
                    .setDescription("A custom prefix for the bot.");
            PROP.register("candy.emoji", "\uD83C\uDF68")
                    .setDisplayName("Server Emoji")
                    .setDescription("The emoji that represents the score.");
            PROP.register("candy.limit", 100)
                    .setDisplayName("Counter Limit")
                    .setDescription("How many messages have to be sent until a point is given.");

            CMD.useCustomPrefixes(Objects.requireNonNull(PROP.getProperty("bot.customprefix")), false);

            API.getThreadPool()
                    .getScheduler()
                    .scheduleAtFixedRate(CandyBot::storeAllData, 5, 5, TimeUnit.MINUTES);
            Runtime.getRuntime().addShutdownHook(new Thread(CandyBot::terminateAll));

            API.updateActivity(ActivityType.LISTENING, "candy!help");
            API.updateStatus(UserStatus.ONLINE);
        } catch (Exception e) {
            throw new RuntimeException("Error in initializer", e);
        }
    }

    public static void main(String[] args) throws Exception {
        CandyBank.INSTANCE.init(API);

        API.addListener(Engine.INSTANCE);
    }

    private static void terminateAll() {
        try {
            CandyBank.INSTANCE.terminate();
            PROP.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void storeAllData() {
        try {
            CandyBank.INSTANCE.storeData();
            PROP.storeData();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
