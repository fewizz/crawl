package ru.fewizz.theotherside;

import net.fabricmc.api.ModInitializer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.fewizz.theotherside.feature.Crawl;
import ru.fewizz.theotherside.feature.Feature;

import java.util.ArrayList;
import java.util.List;

public class Mod implements ModInitializer {
    static final Logger LOGGER = LogManager.getLogger("theotherside");
    static final List<Feature> FEATURES = new ArrayList<>();

    // Even earlier. SUPER HACK
    public static void onPreInitialize() {
        LOGGER.info("Preinit");
        FEATURES.add(new Crawl());

    }

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
        LOGGER.info("Init");
	}
}
