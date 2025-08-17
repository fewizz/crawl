package ru.fewizz.crawl;

import java.util.function.Consumer;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

public class Crawl {

	public record Request(boolean crawl) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<Request> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.parse("crawl:request"));
		public static final StreamCodec<ByteBuf, Request> CODEC = ByteBufCodecs.BOOL.map(Request::new, Request::crawl);

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	};

	public static Consumer<Boolean> sendCrawlRequestPacketToServer = null;

	public static void onCrawlRequestFromClient(Player serverPlayer, boolean value) {
		serverPlayer.getServer().execute(() -> {
			((PlayerExtended) serverPlayer).crawl_setRequestedCrawling(value);
		});
	}

	public static class Shared {
		public static final Pose CRAWLING = Pose.valueOf("CRAWLING");
		public static final EntityDimensions CRAWLING_DIMENSIONS = EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.5F);
	}

}
