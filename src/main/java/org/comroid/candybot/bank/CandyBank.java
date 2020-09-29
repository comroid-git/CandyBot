package org.comroid.candybot.bank;

import org.comroid.common.io.FileHandle;
import org.comroid.mutatio.ref.ReferenceMap;
import org.javacord.api.entity.server.Server;

import java.io.Closeable;
import java.io.IOException;

public final class CandyBank implements Closeable {
    private final ReferenceMap<Long, BankVault> vaults = ReferenceMap.create();
    private final FileHandle vaultsDir;
    private final BankVault globalVault;

    public BankVault getGlobalVault() {
        return globalVault;
    }

    public CandyBank(FileHandle vaultsDir) {
        this.vaultsDir = vaultsDir;
        this.globalVault = makeVault(0);
    }

    public BankVault getVault(Server server) {
        return getVault(server.getId());
    }

    public BankVault getVault(long id) {
        return vaults.wrap(id).orElse(globalVault);
    }

    public BankVault makeVault(long id) {
        if (id != 0 && id < 21154535154122752L)
            throw new IllegalArgumentException("id invalid: " + id);
        if (vaults.process(id).testIfPresent(BankVault::usesGlobalVault))
            return globalVault;
        return vaults.computeIfAbsent(id, () -> {
            FileHandle vaultFile = vaultsDir.createSubFile(String.format("vault-%d.json", id));
            return new BankVault(vaultFile);
        });
    }

    @Override
    public void close() throws IOException {
        // todo
    }
}
