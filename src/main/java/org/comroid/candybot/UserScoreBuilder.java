package org.comroid.candybot;

import discord4j.core.object.entity.User;
import org.comroid.CandyBot;
import org.comroid.candybot.UserScore.Bind;
import org.comroid.varbind.container.DataContainer;
import org.comroid.varbind.container.DataContainerBuilder;

public class UserScoreBuilder extends DataContainerBuilder<UserScoreBuilder, UserScore, CandyBot> {
    public UserScoreBuilder(CandyBot bot) {
        super(UserScore.class, bot);
    }

    public UserScoreBuilder setUser(User user) {
        return with(Bind.User, user.getId().asLong());
    }

    public UserScoreBuilder setScore(int score) {
        return with(Bind.Score, score);
    }

    @Override
    protected UserScore mergeVarCarrier(DataContainer<CandyBot> dataContainer) {
        return new UserScore.Local(dataContainer);
    }
}
