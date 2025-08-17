package ru.fewizz.crawl.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.network.CustomPayloadEvent.Context;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;
import ru.fewizz.crawl.Crawl;
import ru.fewizz.crawl.client.CrawlClient;
import ru.fewizz.crawl.client.OptionsScreen;

@Mod("crawl")
public class CrawlMod {

	public static final SimpleChannel CHANNEL = ChannelBuilder
		.named(Crawl.Request.TYPE.id())
		.simpleChannel()
		.play()
			.serverbound()
				.addMain(
					Boolean.class,
					ByteBufCodecs.BOOL.<RegistryFriendlyByteBuf>cast(),
					(Boolean value, Context ctx) -> {
						Crawl.onCrawlRequestFromClient(ctx.getSender(), value);
					}
				)
		.build();

	@SuppressWarnings("null")
	public CrawlMod(FMLJavaModLoadingContext modContext) {
		Crawl.sendCrawlRequestPacketToServer = (wantsToCrawl) -> {
			Minecraft mc = Minecraft.getInstance();
			CHANNEL.send(wantsToCrawl, mc.getConnection().getConnection());
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
