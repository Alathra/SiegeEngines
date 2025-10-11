package io.github.alathra.siegeengines.translation;

import io.github.alathra.siegeengines.SiegeEngines;
import io.github.alathra.siegeengines.Reloadable;
import io.github.alathra.siegeengines.config.ConfigHandler;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import io.github.milkdrinkers.wordweaver.Translation;
import io.github.milkdrinkers.wordweaver.config.TranslationConfig;

import java.nio.file.Path;

/**
 * A wrapper handler class for handling WordWeaver lifecycle.
 */
public class TranslationHandler implements Reloadable {
    private final ConfigHandler configHandler;

    public TranslationHandler(ConfigHandler configHandler) {
        this.configHandler = configHandler;
    }

    @Override
    public void onLoad(SiegeEngines plugin) {

    }

    @Override
    public void onEnable(SiegeEngines plugin) {
        Translation.initialize(TranslationConfig.builder() // Initialize word-weaver
            .translationDirectory(plugin.getDataPath().resolve("lang"))
            .resourcesDirectory(Path.of("lang"))
            .extractLanguages(true)
            .updateLanguages(true)
            .language(configHandler.getConfig().get("language", "en_US"))
            .defaultLanguage("en_US")
            .componentConverter(s -> ColorParser.of(s).build()) // Use color parser for components by default
            .build()
        );
    }

    @Override
    public void onDisable(SiegeEngines plugin) {
    }
}
