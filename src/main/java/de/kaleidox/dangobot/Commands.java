package de.kaleidox.dangobot;

import java.util.List;
import java.util.Map;

import de.kaleidox.DangoBot;
import de.kaleidox.javacord.util.commands.Command;
import de.kaleidox.javacord.util.embed.DefaultEmbedFactory;
import de.kaleidox.javacord.util.server.properties.PropertyGroup;
import de.kaleidox.javacord.util.ui.messages.PagedEmbed;

import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

@SuppressWarnings("Duplicates")
public enum Commands {
    INSTANCE;

    @Command(aliases = "invite",
            usage = "invite",
            description = "Sends a link to add the bot to your servers!")
    public EmbedBuilder invite(Command.Parameters param) {
        User user = param.getCommandExecutor().flatMap(MessageAuthor::asUser).orElse(null);

        if (user == null) return null;

        return DefaultEmbedFactory.INSTANCE.get()
                .setAuthor(user)
                .setDescription("Invite link: https://discordapp.com/oauth2/authorize?client_id=487745829617139722&scope=bot&permissions=85056");
    }

    @Command(aliases = {"bug", "issues"},
            usage = "bug",
            description = "Sends a link to where you can report bugs!")
    public EmbedBuilder bugs(Command.Parameters param) {
        User user = param.getCommandExecutor().flatMap(MessageAuthor::asUser).orElse(null);

        if (user == null) return null;

        return DefaultEmbedFactory.INSTANCE.get()
                .setAuthor(user)
                .setDescription("GitHub Issue Tracker: https://github.com/burdoto/DangoBot/issues");
    }

    @Command(aliases = {"own", "self"},
            usage = "self",
            description = "Shows your own dango score.",
            enablePrivateChat = false)
    public EmbedBuilder own(Command.Parameters param) {
        User user = param.getCommandExecutor().flatMap(MessageAuthor::asUser).orElse(null);
        Server server = param.getServer().orElseThrow(AssertionError::new);

        if (user == null) return null;

        return DefaultEmbedFactory.INSTANCE.get()
                .setAuthor(user)
                .setDescription("Your score: " + DangoBank.INSTANCE.getScore(server, user) + " "
                        + DangoBot.PROP.getProperty("dango.emoji").getValue(server.getId()).asString()
                );
    }

    @Command(aliases = {"emoji", "setemoji"},
            usage = "emoji [Emoji]",
            description = "Change the emoji of this server!",
            enablePrivateChat = false,
            requiredDiscordPermission = PermissionType.MANAGE_SERVER)
    public EmbedBuilder emoji(Command.Parameters param) {
        User user = param.getCommandExecutor().flatMap(MessageAuthor::asUser).orElse(null);
        Server server = param.getServer().orElseThrow(AssertionError::new);

        if (user == null) return null;

        PropertyGroup property = DangoBot.PROP.getProperty("dango.emoji");

        if (param.getArguments().length == 1) {
            property.setValue(server.getId()).toString(param.getArguments()[0]);

            return DefaultEmbedFactory.INSTANCE.get()
                    .setAuthor(user)
                    .setDescription("Emoji has been set to " + property.getValue(server.getId()).asString());
        }

        return DefaultEmbedFactory.INSTANCE.get()
                .setAuthor(user)
                .setDescription("Current Emoji is " + property.getValue(server.getId()).asString());
    }

    @Command(aliases = {"stats", "scores", "list"},
            usage = "scores",
            description = "Shows the leaderboard for the server!",
            enablePrivateChat = false)
    public Object stats(Command.Parameters param) {
        PagedEmbed pagedEmbed = new PagedEmbed(param.getTextChannel());
        User user = param.getCommandExecutor().flatMap(MessageAuthor::asUser).orElse(null);
        Server server = param.getServer().orElseThrow(AssertionError::new);

        if (user == null) return null;

        Map<Integer, List<User>> best = DangoBank.INSTANCE.getBest(server);
        if (best.size() > 0) {
            int count = 1;
            for (Map.Entry<Integer, List<User>> entry : best.entrySet()) {
                final Integer score = entry.getKey();
                final List<User> users = entry.getValue();
                StringBuilder sb = new StringBuilder();

                users.forEach(usr -> sb.append("- ")
                        .append(usr.getDisplayName(server))
                        .append("\n"));

                pagedEmbed.addField(
                        "__#" + (count++) + "__ - Score: " + score + " "
                                + DangoBot.PROP.getProperty("dango.emoji").getValue(server.getId()).asString(),
                        sb.toString()
                );
            }
        } else return DefaultEmbedFactory.INSTANCE.get()
                .setAuthor(user)
                .setDescription("There are currently no scores!\nStart chatting to earn points!");

        return pagedEmbed;
    }
}
