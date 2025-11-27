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
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
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
					ByteBufCodecs.BOOL.cast(),
					(Boolean value, Context ctx) -> Crawl.onCrawlRequestFromClient(ctx.getSender(), value)
				)
		.build();
    private final ModContainer container;

	@SuppressWarnings("null")
	public CrawlMod(FMLJavaModLoadingContext modContext) {
        this.container = modContext.getContainer();
		Crawl.sendCrawlRequestPacketToServer = (wantsToCrawl) -> {
			Minecraft mc = Minecraft.getInstance();
			CHANNEL.send(wantsToCrawl, mc.getConnection().getConnection());
		};
	}

	@Mod.EventBusSubscriber(modid = "crawl", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public class ClientModEvents {

		@SubscribeEvent
		public void onClientSetup(FMLClientSetupEvent e) {
			CrawlMod.this.container.registerExtensionPoint(
				ConfigScreenFactory.class,
				() -> new ConfigScreenFactory(OptionsScreen::new)
			);
		}

		@SubscribeEvent
		public static void registerBindings(RegisterKeyMappingsEvent event) {
			event.register(CrawlClient.key);
		}

	}

}
