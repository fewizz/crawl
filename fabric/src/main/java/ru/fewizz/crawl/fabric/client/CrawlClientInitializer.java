package ru.fewizz.crawl.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import ru.fewizz.crawl.client.CrawlClient;


public class CrawlClientInitializer implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		KeyMappingHelper.registerKeyMapping(CrawlClient.key);
	}

}
