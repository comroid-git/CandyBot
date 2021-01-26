package org.comroid.candybot.bank;

import org.comroid.common.io.FileHandle;
import org.comroid.crystalshard.entity.guild.Guild;
import org.comroid.mutatio.ref.ReferenceMap;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Predicate;

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

    public BankVault getVault(Guild server) {
        return getVault(server.getID());
    }

    public BankVault getVault(long id) {
        return vaults.wrap(id)
                .filter(((Predicate<BankVault>) BankVault::usesGlobalVault).negate())
                .orElse(globalVault);
    }

    public BankVault makeVault(long id) {
        return vaults.computeIfAbsent(id, () -> {
            FileHandle vaultFile = vaultsDir.createSubFile(String.format("vault-%d.json", id));
            return new BankVault(id, vaultFile);
        });
    }

    @Override
    public void close() throws IOException {
        // todo
        vaults.forEach((id, vault) -> {
            if (vault != null)
                vault.close();
        });
    }
}
