package ru.fewizz.crawl.client;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = "crawl", dist = Dist.CLIENT)
public class CrawlModClient {

	public CrawlModClient(IEventBus modEventBus, ModContainer modContainer) {
		modContainer.registerExtensionPoint(
			IConfigScreenFactory.class,
			(ModContainer mod, Screen parentScreen) -> {
				return new OptionsScreen(parentScreen);
			}
		);
		modEventBus.addListener(CrawlModClient::registerBindings);
	}

	public static void registerBindings(RegisterKeyMappingsEvent event) {
		event.register(CrawlClient.key);
	}

}
