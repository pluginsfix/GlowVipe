package pluginsfix.glowvipe.storage;

import pluginsfix.glowvipe.domain.VaultData;

import java.util.UUID;

public interface VaultRepository {

    VaultData load(UUID playerId);

    void save(VaultData data);

    void saveAll();

    void close();
}
