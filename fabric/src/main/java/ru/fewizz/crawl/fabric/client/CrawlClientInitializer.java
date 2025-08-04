package ru.fewizz.crawl.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import ru.fewizz.crawl.client.CrawlClient;


public class CrawlClientInitializer implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		KeyBindingHelper.registerKeyBinding(CrawlClient.key);
	}

}
