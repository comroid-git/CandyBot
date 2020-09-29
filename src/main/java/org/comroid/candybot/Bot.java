package org.comroid.candybot;

import org.comroid.api.UUIDContainer;
import org.comroid.candybot.bank.CandyBank;
import org.comroid.commandline.CommandLineArgs;
import org.comroid.common.Disposable;
import org.comroid.common.io.FileHandle;
import org.comroid.javacord.util.commands.CommandHandler;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.util.concurrent.CompletableFuture;

public final class Bot extends UUIDContainer.Base implements Disposable {
    public static CommandLineArgs ARGS;
    public static Bot instance;

    public final FileHandle dir;
    public final FileHandle data;
    public final CandyBank candybank;
    public final DiscordApi discord;
    public final CommandHandler cmd;

    private Bot(FileHandle baseDir) {
        this.dir = baseDir;
        this.data = baseDir.createSubDir("data");

        this.candybank = new CandyBank(data.createSubDir("vaults"));

        this.discord = ARGS.process("token")
                .or(data.createSubFile("discord.cred")::getContent)
                .map(token -> new DiscordApiBuilder().setToken(token))
                .map(DiscordApiBuilder::login)
                .into(CompletableFuture::join);
        this.cmd = new CommandHandler(discord);

        addChildren(discord::disconnect, candybank);
    }

    public static void main(String[] args) {
        ARGS = CommandLineArgs.parse(args);

        instance = ARGS.process("dir")
                .or(() -> "/srv/dcb/candybot/")
                .map(path -> new FileHandle(path, true))
                .into(Bot::new);

        Runtime.getRuntime().addShutdownHook(new Thread(instance::close));
    }
}
