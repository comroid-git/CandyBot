package org.comroid.candybot.bank;

import org.comroid.candybot.Bot;
import org.comroid.common.io.FileHandle;
import org.comroid.uniform.ValueType;
import org.comroid.uniform.adapter.json.jackson.JacksonJSONAdapter;
import org.comroid.varbind.FileConfiguration;
import org.comroid.varbind.annotation.RootBind;
import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.bind.VarBind;
import org.javacord.api.entity.server.Server;

public final class BankVault extends FileConfiguration {
    @RootBind
    public static final GroupBind<BankVault> Root
            = new GroupBind<>(JacksonJSONAdapter.instance, "bank-vault");
    public static final VarBind<BankVault, Long, Server, Server> guild
            = Root.createBind("guild")
            .extractAs(ValueType.LONG)
            .andRemap(id -> Bot.instance.discord.getServerById(id).orElse(null))
            .onceEach()
            .setRequired()
            .build();
    public static final VarBind<BankVault, Boolean, Boolean, Boolean> usesGlobal
            = Root.createBind("usesGlobalVault")
            .extractAs(ValueType.BOOLEAN)
            .asIdentities()
            .onceEach()
            .setRequired()
            .build();

    public BankVault(FileHandle vaultFile) {
        super(JacksonJSONAdapter.instance, vaultFile);
    }

    public boolean usesGlobalVault() {
        return requireNonNull(usesGlobal);
    }
}
