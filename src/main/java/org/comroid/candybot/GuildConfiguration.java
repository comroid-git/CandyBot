package org.comroid.candybot;

import discord4j.core.object.entity.Guild;
import discord4j.rest.util.Snowflake;
import org.comroid.CandyBot;
import org.comroid.common.Polyfill;
import org.comroid.common.func.Invocable;
import org.comroid.common.iter.ReferenceIndex;
import org.comroid.common.iter.Span;
import org.comroid.uniform.adapter.json.fastjson.FastJSONLib;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.uniform.node.UniValueNode.ValueType;
import org.comroid.varbind.annotation.Location;
import org.comroid.varbind.annotation.RootBind;
import org.comroid.varbind.bind.ArrayBind;
import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.bind.VarBind;
import org.comroid.varbind.container.DataContainer;
import org.comroid.varbind.container.DataContainerBase;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Location(GuildConfiguration.Bind.class)
public interface GuildConfiguration extends DataContainer<CandyBot> {
    AtomicInteger getCounter();

    default Guild getGuild() {
        return requireNonNull(Bind.Guild);
    }

    default int getLimit() {
        return wrap(Bind.Limit).orElse(100);
    }

    default String getEmoji() {
        return wrap(Bind.Emoji).orElse(CandyBot.DEFAULT_EMOJI);
    }

    default Span<UserScore> getScores() {
        return requireNonNull(Bind.Scores);
    }

    interface Bind {
        @RootBind
        GroupBind<GuildConfiguration, CandyBot> Root
                = new GroupBind<>(FastJSONLib.fastJsonLib, "server_configuration", Invocable.ofConstructor(Polyfill.<Class<GuildConfiguration>>uncheckedCast(Basic.class)));
        VarBind.DependentTwoStage<Long, CandyBot, Guild> Guild
                = Root.bindDependent("guild", ValueType.LONG, (bot, id) -> bot.client.getGuildById(Snowflake.of(id)).block());
        VarBind.OneStage<Integer> Limit
                = Root.bind1stage("limit", ValueType.INTEGER);
        VarBind.OneStage<String> Emoji
                = Root.bind1stage("emoji", ValueType.STRING);
        ArrayBind.DependentTwoStage<UniObjectNode, CandyBot, UserScore, Span<UserScore>> Scores
                = Root.listDependent("scores", UserScore.Bind.Root, () -> new Span<>(ReferenceIndex.of(new LinkedList<>()), Span.ModifyPolicy.SKIP_NULLS));
    }

    final class Basic extends DataContainerBase<CandyBot> implements GuildConfiguration {
        private final AtomicInteger counter;

        @Override
        public AtomicInteger getCounter() {
            return counter;
        }

        public Basic(CandyBot bot, UniObjectNode data) {
            super(data, bot);

            this.counter = new AtomicInteger(0);
        }
    }

    final class Local implements GuildConfiguration, DataContainer.Underlying<CandyBot> {
        private final AtomicInteger counter = new AtomicInteger(0);
        private final DataContainer<CandyBot> underlying;

        @Override
        public AtomicInteger getCounter() {
            return counter;
        }

        @Override
        public DataContainer<CandyBot> getUnderlyingVarCarrier() {
            return underlying;
        }

        public Local(DataContainer<CandyBot> underlying) {
            this.underlying = underlying;
        }
    }
}
