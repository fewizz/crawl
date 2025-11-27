package ru.fewizz.crawl.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;
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
				var server = context.player().getServer();
				server.execute(() -> {
					Crawl.onCrawlRequestFromClient(context.player(), payload.crawl());
				});
			}
		);
		Crawl.sendCrawlRequestPacketToServer = (wantsToCrawl) -> {
			PacketDistributor.sendToServer(new Crawl.Request(wantsToCrawl));
		};
	}

}
