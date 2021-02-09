package org.comroid.candybot.bank;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.comroid.common.Disposable;
import org.comroid.common.io.FileHandle;
import org.comroid.crystalshard.entity.guild.Guild;
import org.comroid.mutatio.ref.ReferenceMap;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
        FileHandle vaultFile = vaultsDir.createSubFile(String.format("vault-%d.json", id));
        if (vaultFile.exists() && !vaults.containsKey(id)) {
            BankVault vault = makeVault(id);
            if (vault.usesGlobalVault())
                return globalVault;
            return vault;
        }
        return vaults.wrap(id)
                .filter(vault -> !vault.usesGlobalVault())
                .orElse(globalVault);
    }

    public BankVault makeVault(long id) {
        FileHandle vaultFile = vaultsDir.createSubFile(String.format("vault-%d.json", id));
        return vaults.computeIfAbsent(id, () -> new BankVault(id, vaultFile));
    }

    private static final Logger logger = LogManager.getLogger();

    @Override
    public void close() throws RuntimeException {
        logger.info("Closing CandyBank");

        final List<Throwable> exceptions = new ArrayList<>();

        vaults.forEach((key, vault) -> {
            try {
                vault.close();
            } catch (Throwable T) {
                exceptions.add(T);
            }
        });

        if (exceptions.size() == 0)
            return;
        if (exceptions.size() == 1)
            throw new RuntimeException(exceptions.get(0));
        throw new Disposable.MultipleExceptions(String.format("Failed to close %d Vaults", exceptions.size()), exceptions);
    }
}
