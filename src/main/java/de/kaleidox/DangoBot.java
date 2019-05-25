package de.kaleidox;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import de.kaleidox.dangobot.DangoBank;
import de.kaleidox.dangobot.Engine;
import de.kaleidox.dangobot.command.AdminCommands;
import de.kaleidox.dangobot.command.BasicCommands;
import de.kaleidox.dangobot.command.DangoCommands;
import de.kaleidox.javacord.util.commands.CommandHandler;
import de.kaleidox.javacord.util.server.properties.ServerPropertiesManager;
import de.kaleidox.javacord.util.ui.embed.DefaultEmbedFactory;
import de.kaleidox.util.files.FileProvider;
import de.kaleidox.util.files.OSValidator;

import org.discordbots.api.client.DiscordBotListAPI;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.util.logging.ExceptionLogger;

public final class DangoBot {
    public final static Color THEME = new Color(0x34A6FF);

    public static final long BOT_ID = 487745829617139722L;

    public static final DiscordApi API;
    public static final CommandHandler CMD;
    public static final ServerPropertiesManager PROP;
    public static final DiscordBotListAPI DBL_API;

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

            if (OSValidator.isUnix()) {
                DBL_API = new DiscordBotListAPI.Builder()
                        .token(new BufferedReader(new FileReader(FileProvider.getFile("login/token_dbl.cred"))).readLine())
                        .botId(API.getYourself().getIdAsString())
                        .build();

                API.addServerJoinListener(event -> DBL_API.setStats(API.getServers().size()));
                API.addServerLeaveListener(event -> DBL_API.setStats(API.getServers().size()));
            } else DBL_API = null;

            DefaultEmbedFactory.setEmbedSupplier(() -> new EmbedBuilder().setColor(THEME));

            CMD = new CommandHandler(API);
            CMD.prefixes = new String[]{"dango!", "d!"};
            CMD.useDefaultHelp(null);
            CMD.registerCommands(BasicCommands.INSTANCE);
            CMD.registerCommands(DangoCommands.INSTANCE);
            CMD.registerCommands(AdminCommands.INSTANCE);

            PROP = new ServerPropertiesManager(FileProvider.getFile("data/serverProps.json"));
            PROP.usePropertyCommand(null, CMD);
            PROP.register("bot.customprefix", "!dango ")
                    .setDisplayName("Custom Prefix")
                    .setDescription("A custom prefix for the bot.");
            PROP.register("dango.emoji", "\uD83C\uDF61")
                    .setDisplayName("Server Emoji")
                    .setDescription("The emoji that represents the score.");
            PROP.register("dango.limit", 100)
                    .setDisplayName("Counter Limit")
                    .setDescription("How many messages have to be sent until a point is given.");

            CMD.useCustomPrefixes(Objects.requireNonNull(PROP.getProperty("bot.customprefix")), false);

            API.getThreadPool()
                    .getScheduler()
                    .scheduleAtFixedRate(DangoBot::storeAllData, 5, 5, TimeUnit.MINUTES);
            Runtime.getRuntime().addShutdownHook(new Thread(DangoBot::terminateAll));

            API.updateActivity(ActivityType.LISTENING, "dango!help");
            API.updateStatus(UserStatus.ONLINE);
        } catch (Exception e) {
            throw new RuntimeException("Error in initializer", e);
        }
    }

    public static void main(String[] args) throws Exception {
        DangoBank.INSTANCE.init(API);

        API.addListener(Engine.INSTANCE);
    }

    private static void terminateAll() {
        try {
            DangoBank.INSTANCE.terminate();
            PROP.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void storeAllData() {
        try {
            DangoBank.INSTANCE.storeData();
            PROP.storeData();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
