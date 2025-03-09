package ru.fewizz.crawl.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;


public class CrawlClientInitializer implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		KeyBindingHelper.registerKeyBinding(CrawlClient.key);
	}

}
