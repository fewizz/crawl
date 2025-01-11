package ru.fewizz.crawl;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class CrawlInitializer implements ModInitializer {

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playC2S().register(Crawl.Payload.ID, Crawl.Payload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(Crawl.Payload.ID, (payload, context) -> {
			context.player().server.execute(() -> context.player().getEntityData().set(Crawl.Shared.CRAWL_REQUEST, payload.crawl()));
		});

		Crawl.crawlRequestPacket = (wantsToCrawl) -> {
			ClientPlayNetworking.send(new Crawl.Payload(wantsToCrawl));
		};
	}

}
