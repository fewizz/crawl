package ru.fewizz.crawl;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(value = "crawl")
public class CrawlMod {

	public CrawlMod(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.addListener(CrawlMod::registerPacket);
	}

	@SuppressWarnings("null")
	public static void registerPacket(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar("1");
		registrar.playToServer(
			Crawl.Payload.ID,
			Crawl.Payload.CODEC,
			(payload, context) -> {
				var server = context.player().getServer();
				server.execute(() -> {
					Crawl.setEntityRequestingCrawling.accept(context.player(), payload.crawl());
				});
			}
		);
		Crawl.crawlRequestPacket = (wantsToCrawl) -> {
			ClientPacketDistributor.sendToServer(new Crawl.Payload(wantsToCrawl));
		};
	}

}
