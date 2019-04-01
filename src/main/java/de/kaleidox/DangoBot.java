package de.kaleidox;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import de.kaleidox.dangobot.Commands;
import de.kaleidox.dangobot.DangoBank;
import de.kaleidox.dangobot.Engine;
import de.kaleidox.javacord.util.commands.CommandHandler;
import de.kaleidox.javacord.util.embed.DefaultEmbedFactory;
import de.kaleidox.javacord.util.server.properties.ServerPropertiesManager;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.util.logging.ExceptionLogger;

public final class DangoBot {
    public final static Color THEME = new Color(0x34A6FF);

    public static final DiscordApi API;
    public static final CommandHandler CMD;
    public static final ServerPropertiesManager PROP;

    static {
        StringBuilder token = new StringBuilder();

        try {
            File tokenFile = new File("data/token.cred");
            System.out.println("Looking for token file at: " + tokenFile.getAbsolutePath());
            FileInputStream stream = new FileInputStream(tokenFile);
            int r;
            while ((r = stream.read()) != -1) token.append((char) r);
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred while reading token file", e);
        }

        API = new DiscordApiBuilder()
                .setToken(token.toString())
                .setWaitForServersOnStartup(true)
                .login()
                .exceptionally(ExceptionLogger.get())
                .join();

        API.updateStatus(UserStatus.DO_NOT_DISTURB);
        API.updateActivity("Booting up...");

        try {
            File file = new File("data/serverProps.json");
            file.createNewFile();
            PROP = new ServerPropertiesManager(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        API.getThreadPool()
                .getScheduler()
                .scheduleAtFixedRate(() -> {
                    try {
                        PROP.storeData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, 5, 5, TimeUnit.MINUTES);

        CMD = new CommandHandler(API);
        CMD.autoDeleteResponseOnCommandDeletion = true;
        CMD.prefixes = new String[]{"d!", "!dango "};
        CMD.useDefaultHelp(null);
        CMD.registerCommands(Commands.INSTANCE);

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
        PROP.usePropertyCommand(null, CMD);

        DefaultEmbedFactory.setEmbedSupplier(() -> new EmbedBuilder().setColor(THEME));
    }

    public static void main(String[] args) throws Exception {
        DangoBank.INSTANCE.init(API);

        API.addListener(Engine.INSTANCE);

        Runtime.getRuntime().addShutdownHook(new Thread(DangoBot::terminateAll));
        API.getThreadPool().getScheduler().scheduleAtFixedRate(DangoBot::storeAllData, 5, 5, TimeUnit.MINUTES);

        API.updateActivity(ActivityType.LISTENING, "to d!help");
        API.updateStatus(UserStatus.ONLINE);
    }

    private static void terminateAll() {
        try {
            DangoBank.INSTANCE.terminate();
            PROP.terminate();
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
