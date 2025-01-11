package ru.fewizz.crawl;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(value = "crawl")
public class CrawlMod {

	public CrawlMod(IEventBus modEventBus, ModContainer modContainer) {
	}

	@EventBusSubscriber(modid = "crawl", bus = EventBusSubscriber.Bus.MOD)
	public static class CommonEvents {

		@SubscribeEvent
		@SuppressWarnings("null")
		public static void registerPacket(RegisterPayloadHandlersEvent event) {
			PayloadRegistrar registrar = event.registrar("1");
			registrar.playToServer(
				Crawl.Payload.ID,
				Crawl.Payload.CODEC,
				(payload, context) -> {
					var server = context.player().getServer();
					server.execute(() -> context.player().getEntityData().set(Crawl.Shared.CRAWL_REQUEST, payload.crawl()));
				}
			);
			Crawl.crawlRequestPacket = (wantsToCrawl) -> {
				PacketDistributor.sendToServer(new Crawl.Payload(wantsToCrawl));
			};
		}

	}

	@EventBusSubscriber(modid = "crawl", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientEvents {

		@SubscribeEvent
		public static void registerBindings(RegisterKeyMappingsEvent event) {
			event.register(CrawlClient.key);
		}

	}

}
