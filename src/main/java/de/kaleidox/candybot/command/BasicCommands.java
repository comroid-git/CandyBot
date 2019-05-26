package de.kaleidox.candybot.command;

import de.kaleidox.javacord.util.commands.Command;
import de.kaleidox.javacord.util.commands.CommandGroup;
import de.kaleidox.javacord.util.ui.embed.DefaultEmbedFactory;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import static org.javacord.api.util.logging.ExceptionLogger.get;

@CommandGroup(name = "Basic Commands")
public enum BasicCommands {
    INSTANCE;

    public static final String INVITE_LINK = "https://discordapp.com/oauth2/authorize?client_id=487745829617139722&scope=bot&permissions=85056";

    @Command(usage = "about", description = "Information around the bot!")
    public void about(User user) {
        if (user == null) return;

        user.sendMessage(DefaultEmbedFactory.INSTANCE.get()
                .setAuthor(user)
                .addField("Bot Invite Link", INVITE_LINK)
                .addField("GitHub Issue Tracker", "https://github.com/burdoto/CandyBot/issues")
                .addField("Support Discord server", "https://discord.gg/fGNcvNY"));
    }

    @Command(usage = "invite", description = "Sends a link to add the bot to your servers!")
    public void invite(Message msg, User usr) {
        usr.sendMessage(DefaultEmbedFactory.create().addField("Invite Link", INVITE_LINK))
                .thenCompose(ign -> msg.delete())
                .exceptionally(get())
                .join();
    }

    @Command(aliases = {"bug", "issues"}, usage = "bug", description = "Sends a link to where you can report bugs!")
    public EmbedBuilder bugs(User user) {
        if (user == null) return null;

        return DefaultEmbedFactory.INSTANCE.get()
                .setAuthor(user)
                .setDescription("GitHub Issue Tracker: https://github.com/burdoto/CandyBot/issues");
    }
}
