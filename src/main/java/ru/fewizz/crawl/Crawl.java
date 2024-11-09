package ru.fewizz.crawl;

import io.netty.buffer.ByteBuf;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class Crawl implements ModInitializer {

	public record Payload(boolean crawl) implements CustomPayload {
		public static final CustomPayload.Id<Payload> ID = new CustomPayload.Id<>(CRAWL_ID);
		public static final PacketCodec<ByteBuf, Payload> CODEC = PacketCodecs.BOOL.xmap(Payload::new, Payload::crawl);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	};

	public static final Identifier CRAWL_ID = Identifier.of("crawl:identifier");

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playC2S().register(Payload.ID, Payload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(Payload.ID, (payload, context) -> {
			context.player().server.execute(() -> context.player().getDataTracker().set(Shared.CRAWL_REQUEST, payload.crawl));
		});
	}
	
	public static class Shared {
		public static final EntityPose CRAWLING = EntityPose.valueOf("CRAWLING");
		public static final EntityDimensions CRAWLING_DIMENSIONS = EntityDimensions.changing(0.6F, 0.6F).withEyeHeight(0.6F);
		public static final TrackedData<Boolean> CRAWL_REQUEST = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	}

}
