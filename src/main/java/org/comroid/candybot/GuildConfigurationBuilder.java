package org.comroid.candybot;

import discord4j.core.object.entity.Guild;
import org.comroid.CandyBot;
import org.comroid.candybot.GuildConfiguration.Bind;
import org.comroid.varbind.container.DataContainer;
import org.comroid.varbind.container.DataContainerBuilder;

public class GuildConfigurationBuilder extends DataContainerBuilder<GuildConfigurationBuilder, GuildConfiguration, CandyBot> {
    public GuildConfigurationBuilder(CandyBot bot) {
        super(GuildConfiguration.class, bot);
    }

    public GuildConfigurationBuilder setGuild(Guild guild) {
        return with(Bind.Guild, guild.getId().asLong());
    }

    public GuildConfigurationBuilder setLimit(int limit) {
        return with(Bind.Limit, limit);
    }

    public GuildConfigurationBuilder setEmoji(String emoji) {
        return with(Bind.Emoji, emoji);
    }

    @Override
    protected GuildConfiguration mergeVarCarrier(DataContainer<CandyBot> dataContainer) {
        return new GuildConfiguration.Local(dataContainer);
    }
}
