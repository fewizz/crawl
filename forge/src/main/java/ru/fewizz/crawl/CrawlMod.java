package ru.fewizz.crawl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkProtocol;
import net.minecraftforge.network.SimpleChannel;
import ru.fewizz.crawl.client.CrawlClient;
import ru.fewizz.crawl.client.OptionsScreen;

@Mod("crawl")
public class CrawlMod {

	public static final SimpleChannel CHANNEL = ChannelBuilder.named("crawl:channel").simpleChannel();

	@SuppressWarnings("null")
	public CrawlMod(FMLJavaModLoadingContext modContext) {
		CHANNEL.messageBuilder(Crawl.Payload.class, NetworkProtocol.PLAY)
			// can't set codec directly because of FriendlyByteBuf
			.encoder((msg, buf) -> Crawl.Payload.CODEC.encode(buf, msg))
			.decoder(buf -> Crawl.Payload.CODEC.decode(buf))
			.consumerMainThread((payload, context) -> {
				context.getSender().getEntityData().set(
					Crawl.Shared.CRAWL_REQUEST,
					payload.crawl()
				);
			})
			.direction(PacketFlow.SERVERBOUND)
			.add();
		CHANNEL.build();

		Crawl.crawlRequestPacket = (wantsToCrawl) -> {
			Minecraft mc = Minecraft.getInstance();
			CHANNEL.send(new Crawl.Payload(wantsToCrawl), mc.getConnection().getConnection());
		};
	}

	@Mod.EventBusSubscriber(modid = "crawl", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents {

		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent e) {
			var container = ModList.get().getModContainerById("crawl").get();
			container.registerExtensionPoint(
				ConfigScreenFactory.class,
				() -> { return new ConfigScreenFactory((Screen parentScreen) -> {
					return new OptionsScreen(parentScreen);
				});}
			);
		}

		@SubscribeEvent
		public static void registerBindings(RegisterKeyMappingsEvent event) {
			event.register(CrawlClient.key);
		}

	}

}
