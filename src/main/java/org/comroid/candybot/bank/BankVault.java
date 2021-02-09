package org.comroid.candybot.bank;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.comroid.api.Named;
import org.comroid.api.UncheckedCloseable;
import org.comroid.candybot.CandyBot;
import org.comroid.common.io.FileHandle;
import org.comroid.crystalshard.entity.guild.Guild;
import org.comroid.crystalshard.entity.user.User;
import org.comroid.uniform.adapter.json.fastjson.FastJSONLib;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;

import java.util.concurrent.atomic.AtomicInteger;

public final class BankVault implements Named, UncheckedCloseable {
    private final AtomicInteger counter = new AtomicInteger(0);
    private final long id;
    private final FileHandle file;
    private final UniObjectNode data;
    private final UniObjectNode accounts;

    @Override
    public String getName() {
        return id == 0 ? "Global" : CandyBot.instance
                .getCache()
                .getGuild(id)
                .map(Guild::getName)
                .assertion();
    }

    public BankVault(long id, FileHandle file) {
        this.id = id;
        this.file = file;
        this.data = FastJSONLib.fastJsonLib.createUniObjectNode();
        this.accounts = data.putObject("accounts");
    }

    public void setEmoji(String emoji) {
        if (id == 0)
            throw new UnsupportedOperationException("Global can't be changed");
        data.put("emoji", emoji);
    }

    public String getEmoji() {
        if (!data.containsKey("emoji"))
            data.put("emoji", "\uD83C\uDF61");
        return data.get("emoji").asString("\uD83C\uDF61");
    }

    public void setUseGlobalVault(boolean state) {
        if (id == 0)
            throw new UnsupportedOperationException("Global can't be changed");
        data.put("usesGlobalVault", state);
    }

    public boolean usesGlobalVault() {
        if (!data.containsKey("usesGlobalVault"))
            data.put("usesGlobalVault", true);
        return data.get("usesGlobalVault").asBoolean();
    }

    public void setLimit(int limit) {
        if (id == 0)
            throw new UnsupportedOperationException("Global can't be changed");
        data.put("limit", limit);
    }

    public int getLimit() {
        if (!data.containsKey("limit"))
            data.put("limit", 100);
        return data.get("limit").asInt(100);
    }

    public int getBalance(User user) {
        final String key = String.valueOf(user.getID());
        if (!accounts.containsKey(key))
            accounts.put(key, 0);
        return accounts.get(key).asInt(0);
    }

    public int counter() {
        return counter.get();
    }

    public boolean countUp() {
        return counter.accumulateAndGet(1, Integer::sum) >= getLimit();
    }

    public int winner(User user) {
        counter.set(0);
        return increment(user);
    }

    public int increment(User user) {
        final UniNode node = accounts.get(String.valueOf(user.getID()));
        node.asValueNode().compute(x -> ((int) x) + 1);
        return node.asInt();
    }

    @Override
    public void close() {
        file.setContent(data.toSerializedString());
        logger.debug("Vault {} stored data", id);
    }

    private static final Logger logger = LogManager.getLogger();
}
