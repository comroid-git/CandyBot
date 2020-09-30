package org.comroid.candybot;

import org.comroid.botsuite.javacord.JavacordBotBase;
import org.comroid.commandline.CommandLineArgs;
import org.comroid.common.io.FileHandle;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

public final class Bot extends JavacordBotBase {
    public static final FileHandle DIR_DATA = new FileHandle("/srv/dcb/candybot/", true);
    public static final FileHandle DIR_LOGIN = DIR_DATA.createSubDir("login");
    public static CommandLineArgs ARGS;
    public static Bot instance;

    private Bot(DiscordApi api) {
        super(DIR_DATA, api);
    }

    public static void main(String[] args) {
        ARGS = CommandLineArgs.parse(args);

        DiscordApi api = new DiscordApiBuilder()
                .setToken(ARGS.wrap("token").orElseGet(DIR_LOGIN.createSubFile("discord.cred")::getContent))
                .login()
                .join();
        instance = new Bot(api);
    }
}
