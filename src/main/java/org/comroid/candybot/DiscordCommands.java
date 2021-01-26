package org.comroid.candybot;

import org.comroid.candybot.bank.BankVault;
import org.comroid.candybot.bank.CandyBank;
import org.comroid.crystalshard.entity.guild.Guild;
import org.comroid.crystalshard.entity.user.User;
import org.comroid.crystalshard.model.command.CommandOptionChoice;
import org.comroid.crystalshard.ui.annotation.Option;
import org.comroid.crystalshard.ui.annotation.SlashCommand;

public final class DiscordCommands {
    private DiscordCommands() {
        throw new UnsupportedOperationException();
    }

    @SlashCommand(name = "stats", description = "Statistic related Commands", useGlobally = false)
    public static final class StatsBlob {
        @SlashCommand(name = "own", description = "See own Candy stats")
        public static String own(User user, Guild guild) {
            BankVault vault = CandyBot.CANDY_BANK.getVault(guild);
            int balance = vault.getBalance(user);

            return String.format("%s, your balance in %s is %d", user.getDisplayName(guild), vault.getName(), balance);
        }

        @SlashCommand(name = "of", description = "See stats of User")
        public static String of(@Option(name = "user", required = true) User user, Guild guild) {
            BankVault vault = CandyBot.CANDY_BANK.getVault(guild);
            int balance = vault.getBalance(user);

            return String.format("Balance of %s in %s is %d", user.getDisplayName(guild), vault.getName(), balance);
        }
    }

    @SlashCommand(name = "candy", description = "CandyBot Configuration related Commands", useGlobally = false)
    public static final class CandyBlob {
        @SlashCommand(name = "emoji", description = "Emoji Configuration")
        public static final class EmojiBlob {
            @SlashCommand(name = "get", description = "Check the currently defined Emoji")
            public static String get(Guild guild) {
                BankVault vault = CandyBot.CANDY_BANK.getVault(guild);
                return String.format("%s uses this emoji: %s", vault.getName(), vault.getEmoji());
            }

            @SlashCommand(name = "set", description = "Set a new Emoji")
            public static String set(Guild guild, @Option(name = "emoji", description = "The Emoji", required = true) String emoji) {
                BankVault vault = CandyBot.CANDY_BANK.getVault(guild);
                vault.setEmoji(emoji);
                return String.format("Emojis of %s changed to: %s", vault.getName(), vault.getEmoji());
            }

            @SlashCommand(name = "reset", description = "Resets the emoji back to default value")
            public static String reset(Guild guild) {
                BankVault vault = CandyBot.CANDY_BANK.getVault(guild);
                vault.setEmoji("\uD83C\uDF61");
                return String.format("Emoji of %s changed back to default: %s", vault.getName(), vault.getEmoji());
            }
        }

        @SlashCommand(name = "limit", description = "Limit Configuration")
        public static final class LimitBlob {
            @SlashCommand(name = "get", description = "Check the currently defined Limit")
            public static String get(Guild guild) {
                BankVault vault = CandyBot.CANDY_BANK.getVault(guild);
                return String.format("%s uses the limit %d", vault.getName(), vault.getLimit());
            }

            @SlashCommand(name = "set", description = "Set a new Limit")
            public static String set(Guild guild, @Option(name = "limit", description = "The Limit", required = true) int limit) {
                BankVault vault = CandyBot.CANDY_BANK.getVault(guild);
                vault.setLimit(limit);
                return String.format("Limit of %s changed to %d", vault.getName(), vault.getLimit());
            }

            @SlashCommand(name = "reset", description = "Resets the Limit back to default value")
            public static String reset(Guild guild) {
                BankVault vault = CandyBot.CANDY_BANK.getVault(guild);
                vault.setLimit(100);
                return String.format("Limit of %s changed back to default: %d", vault.getName(), vault.getLimit());
            }
        }

        @SlashCommand(name = "global-override", description = "Global Override Configuration")
        public static final class GlobalBlob {
            @SlashCommand(name = "enable", description = "Enable Global Overriding")
            public static void enable(Guild guild) {
                BankVault vault = CandyBot.CANDY_BANK.makeVault(guild.getID());
                vault.setUseGlobalVault(true);
            }

            @SlashCommand(name = "disable", description = "Disable Global Overriding")
            public static void disable(Guild guild) {
                BankVault vault = CandyBot.CANDY_BANK.makeVault(guild.getID());
                vault.setUseGlobalVault(false);
            }
        }
    }

    @SlashCommand(name = "dev", useGlobally = false)
    public static final class DevCommands {
        @SlashCommand
        public static void shutdown(User user) {
            if (user.getID() == 141476933849448448L)
                System.exit(0);
        }

        @SlashCommand
        public static String counter(Guild guild) {
            BankVault vault = CandyBot.CANDY_BANK.getVault(guild);
            return "counter = " + vault.counter();
        }
    }
}
