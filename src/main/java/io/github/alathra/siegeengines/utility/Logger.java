package io.github.alathra.siegeengines.utility;


import io.github.alathra.siegeengines.SiegeEngines;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

/**
 * A class that provides shorthand access to {@link SiegeEngines#getComponentLogger}.
 */
public class Logger {
    /**
     * Get component logger. Shorthand for:
     *
     * @return the component logger {@link SiegeEngines#getComponentLogger}.
     */
    @NotNull
    public static ComponentLogger get() {
        return SiegeEngines.getInstance().getComponentLogger();
    }
}
