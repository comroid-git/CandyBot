package de.kaleidox.dangobot.command;

import de.kaleidox.javacord.util.commands.Command;
import de.kaleidox.javacord.util.commands.CommandGroup;
import de.kaleidox.javacord.util.ui.embed.DefaultEmbedFactory;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

@CommandGroup(name = "Basic Commands")
public enum BasicCommands {
    INSTANCE;

    @Command(usage = "about", description = "Information around the bot!")
    public void about(User user) {
        if (user == null) return;

        user.sendMessage(DefaultEmbedFactory.INSTANCE.get()
                .setAuthor(user)
                .addField("Bot Invite Link", "https://discordapp.com/oauth2/authorize?client_id=487745829617139722&scope=bot&permissions=85056")
                .addField("GitHub Issue Tracker", "https://github.com/burdoto/DangoBot/issues")
                .addField("Support Discord server", "https://discord.gg/fGNcvNY"));
    }

    @Command(usage = "invite", description = "Sends a link to add the bot to your servers!")
    public void invite(User user) {
        if (user == null) return;

        user.sendMessage(DefaultEmbedFactory.INSTANCE.get()
                .setAuthor(user)
                .setDescription("Invite link: https://discordapp.com/oauth2/authorize?client_id=487745829617139722&scope=bot&permissions=85056"));
    }

    @Command(aliases = {"bug", "issues"}, usage = "bug", description = "Sends a link to where you can report bugs!")
    public EmbedBuilder bugs(User user) {
        if (user == null) return null;

        return DefaultEmbedFactory.INSTANCE.get()
                .setAuthor(user)
                .setDescription("GitHub Issue Tracker: https://github.com/burdoto/DangoBot/issues");
    }
}
