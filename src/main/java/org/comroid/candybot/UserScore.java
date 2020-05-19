package org.comroid.candybot;

import discord4j.core.object.entity.User;
import discord4j.rest.util.Snowflake;
import org.comroid.CandyBot;
import org.comroid.uniform.adapter.json.fastjson.FastJSONLib;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.uniform.node.UniValueNode.ValueType;
import org.comroid.varbind.annotation.Location;
import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.bind.VarBind;
import org.comroid.varbind.container.DataContainer;
import org.comroid.varbind.container.DataContainerBase;

import java.util.Comparator;

@Location(UserScore.Bind.class)
public interface UserScore extends DataContainer<CandyBot>, Comparable<UserScore> {
    Comparator<UserScore> USER_SCORE_COMPARATOR = Comparator.comparingInt(UserScore::getScore).reversed();

    default User getUser() {
        return requireNonNull(Bind.User);
    }

    default int getScore() {
        return requireNonNull(Bind.Score);
    }

    @Override
    default int compareTo(UserScore other) {
        return USER_SCORE_COMPARATOR.compare(this, other);
    }

    interface Bind {
        GroupBind<UserScore, CandyBot> Root
                = new GroupBind<>(FastJSONLib.fastJsonLib, "user_score");
        VarBind.DependentTwoStage<Long, CandyBot, User> User
                = Root.bindDependent("user", ValueType.LONG, (bot, id) -> bot.client.getUserById(Snowflake.of(id)).block());
        VarBind.OneStage<Integer> Score
                = Root.bind1stage("score", ValueType.INTEGER);
    }

    final class Basic extends DataContainerBase<CandyBot> implements UserScore {
        public Basic(CandyBot bot, UniObjectNode data) {
            super(data, bot);
        }
    }
}
