package ru.fewizz.crawl.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import ru.fewizz.crawl.Crawl;

@Mod(value = "crawl")
public class CrawlMod {

	public CrawlMod(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.addListener(CrawlMod::registerPacket);
	}

	@SuppressWarnings("null")
	public static void registerPacket(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar("1");
		registrar.playToServer(
			Crawl.Request.TYPE,
			Crawl.Request.CODEC,
			(payload, context) -> {
				var server = context.player().level().getServer();
				server.execute(() -> {
					Crawl.onCrawlRequestFromClient(context.player(), server, payload.crawl());
				});
			}
		);
		Crawl.sendCrawlRequestPacketToServer = (wantsToCrawl) -> {
			ClientPacketDistributor.sendToServer(new Crawl.Request(wantsToCrawl));
		};
	}

}
