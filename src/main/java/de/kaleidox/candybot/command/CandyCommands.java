package de.kaleidox.candybot.command;

import java.util.List;
import java.util.Map;

import de.kaleidox.CandyBot;
import de.kaleidox.candybot.CandyBank;
import de.comroid.javacord.util.commands.Command;
import de.comroid.javacord.util.commands.CommandGroup;
import de.comroid.javacord.util.ui.embed.DefaultEmbedFactory;
import de.comroid.javacord.util.ui.messages.paging.PagedEmbed;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

@CommandGroup(name = "Candy Commands", description = "Commands for interacting with CandyBot")
public enum CandyCommands {
    INSTANCE;

    @Command(aliases = {"own", "self"},
            usage = "self",
            description = "Shows your own candy score.",
            enablePrivateChat = false)
    public EmbedBuilder own(Server server, User user) {
        if (user == null) return null;

        return DefaultEmbedFactory.INSTANCE.get()
                .setAuthor(user)
                .setDescription("Your score: " + CandyBank.INSTANCE.getScore(server, user) + " "
                        + CandyBot.PROP.getProperty("candy.emoji").getValue(server.getId()).asString()
                );
    }

    @Command(aliases = {"stats", "scores", "list"},
            usage = "scores",
            description = "Shows the leaderboard for the server!",
            enablePrivateChat = false)
    public Object stats(Server server, User user, ServerTextChannel textChannel) {
        if (user == null) return null;
        PagedEmbed pagedEmbed = new PagedEmbed(textChannel);

        Map<Integer, List<User>> best = CandyBank.INSTANCE.getBest(server);
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
                                + CandyBot.PROP.getProperty("candy.emoji").getValue(server.getId()).asString(),
                        sb.toString()
                );
            }
        } else return DefaultEmbedFactory.INSTANCE.get()
                .setAuthor(user)
                .setDescription("There are currently no scores!\nStart chatting to earn points!");

        return pagedEmbed;
    }
}
