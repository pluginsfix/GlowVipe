package pluginsfix.glowvipe.hook;

import java.util.UUID;

public interface CurrencyHook {

    boolean isAvailable();

    boolean has(UUID playerId, int amount);

    boolean withdraw(UUID playerId, int amount);
}
