package ru.fewizz.crawl;

import io.netty.buffer.ByteBuf;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

public class Crawl implements ModInitializer {

	public record Payload(boolean crawl) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<Payload> ID = new CustomPacketPayload.Type<>(CRAWL_ID);
		public static final StreamCodec<ByteBuf, Payload> CODEC = ByteBufCodecs.BOOL.map(Payload::new, Payload::crawl);

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	};

	public static final ResourceLocation CRAWL_ID = ResourceLocation.parse("crawl:identifier");

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playC2S().register(Payload.ID, Payload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(Payload.ID, (payload, context) -> {
			context.player().server.execute(() -> context.player().getEntityData().set(Shared.CRAWL_REQUEST, payload.crawl));
		});
	}

	public static class Shared {
		public static final Pose CRAWLING = Pose.valueOf("CRAWLING");
		public static final EntityDimensions CRAWLING_DIMENSIONS = EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.6F);
		public static final EntityDataAccessor<Boolean> CRAWL_REQUEST = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
	}

}
