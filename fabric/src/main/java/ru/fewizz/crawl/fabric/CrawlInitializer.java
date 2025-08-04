package ru.fewizz.crawl.fabric;

import com.mojang.serialization.Codec;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import ru.fewizz.crawl.Crawl;

public class CrawlInitializer implements ModInitializer {

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playC2S().register(Crawl.Payload.ID, Crawl.Payload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(Crawl.Payload.ID, (payload, context) -> {
			context.player().getServer().execute(() -> {
				Crawl.setEntityRequestingCrawling.accept(context.player(), payload.crawl());
			});
		});

		Crawl.crawlRequestPacket = (wantsToCrawl) -> {
			ClientPlayNetworking.send(new Crawl.Payload(wantsToCrawl));
		};

		var attachment = AttachmentRegistry.<Boolean>create(
			ResourceLocation.fromNamespaceAndPath("crawl", "crawling"),
			builder -> {
				builder.initializer(() -> Boolean.FALSE);
				builder.persistent(Codec.BOOL);
				builder.syncWith(
					new StreamCodec<RegistryFriendlyByteBuf, Boolean>() {

						@Override
						public Boolean decode(RegistryFriendlyByteBuf buff) {
							return buff.getBoolean(0);
						}

						@Override
						public void encode(RegistryFriendlyByteBuf buff, Boolean value) {
							buff.setBoolean(0, value);
						}
						
					},
					AttachmentSyncPredicate.all()
				);
			}
		);

		Crawl.isEntityRequestingCrawling = (Entity e) -> {
			return e.getAttachedOrElse(attachment, Boolean.FALSE);
		};
		Crawl.setEntityRequestingCrawling = (Entity e, Boolean value) -> {
			e.setAttached(attachment, value);
		};
	}

}
