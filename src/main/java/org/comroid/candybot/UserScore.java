package org.comroid.candybot;

import discord4j.core.object.entity.User;
import discord4j.rest.util.Snowflake;
import org.comroid.CandyBot;
import org.comroid.common.Polyfill;
import org.comroid.common.func.Invocable;
import org.comroid.uniform.adapter.json.fastjson.FastJSONLib;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.uniform.node.UniValueNode.ValueType;
import org.comroid.varbind.annotation.Location;
import org.comroid.varbind.annotation.RootBind;
import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.bind.ReBind;
import org.comroid.varbind.bind.VarBind;
import org.comroid.varbind.container.DataContainer;
import org.comroid.varbind.container.DataContainerBase;
import reactor.core.publisher.Mono;

import java.util.Comparator;

@Location(UserScore.Bind.class)
public interface UserScore extends DataContainer<CandyBot>, Comparable<UserScore> {
    Comparator<UserScore> USER_SCORE_COMPARATOR = Comparator.comparingInt(UserScore::getScore).reversed();

    default User getUser() {
        return requireNonNull(Bind.User, "No user found with ID " + getExtractionReference(Bind.User)
                .requireNonNull("Could not extract")
                .requireNonNull("No ID present"));
    }

    default int getScore() {
        return requireNonNull(Bind.Score);
    }

    @Override
    default int compareTo(UserScore other) {
        return USER_SCORE_COMPARATOR.compare(this, other);
    }

    interface Bind {
        @RootBind
        GroupBind<UserScore, CandyBot> Root
                = new GroupBind<>(FastJSONLib.fastJsonLib, "user_score", Invocable.ofConstructor(Polyfill.<Class<UserScore>>uncheckedCast(Basic.class)));
        VarBind.OneStage<Long> UserId
                = Root.bind1stage("user", ValueType.LONG);
        ReBind.DependentTwoStage<Long, CandyBot, User> User
                = UserId.rebindDependent((id, bot) -> {
            final Snowflake snowflake = Snowflake.of(id);
            final Mono<discord4j.core.object.entity.User> userById = bot.client.getUserById(snowflake);
            final Mono<discord4j.core.object.entity.User> log = userById.log();
            final discord4j.core.object.entity.User block = log.block();
            return block;
        });
        VarBind.OneStage<Integer> Score
                = Root.bind1stage("score", ValueType.INTEGER);
    }

    final class Basic extends DataContainerBase<CandyBot> implements UserScore {
        public Basic(CandyBot bot, UniObjectNode data) {
            super(data, bot);
        }
    }

    final class Local implements UserScore, DataContainer.Underlying<CandyBot> {
        private final DataContainer<CandyBot> underlying;

        @Override
        public DataContainer<CandyBot> getUnderlyingVarCarrier() {
            return underlying;
        }

        public Local(DataContainer<CandyBot> underlying) {
            this.underlying = underlying;
        }
    }
}
