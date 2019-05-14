package ru.fewizz.crawl;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.fabricmc.fabric.impl.network.ServerSidePacketRegistryImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class CrawlMod implements ModInitializer {
    public static final Identifier CRAWL_IDENTIFIER = new Identifier("crawl:identifier");
    
	@Override
	public void onInitialize() {
		registerListener();
	}
	
	void registerListener() {
		ServerSidePacketRegistryImpl.INSTANCE.register(CRAWL_IDENTIFIER, (context, buf) -> {
			boolean val = buf.readBoolean();
			context.getTaskQueue().execute(() -> {
				context.getPlayer().getDataTracker().set(Shared.CRAWLING_REQUEST, val);
			});
		});
	}
	
	public static class Shared {
		public static final EntityPose CRAWLING = Enum.valueOf(EntityPose.class, "CRAWLING");//EnumHack.addEnum(EntityPose.class, "CRAWLING");
		public static final EntitySize CRAWLING_SIZE = new EntitySize(0.6F, 0.6F, false);
		public static final TrackedData<Boolean> CRAWLING_REQUEST = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	}
	
	@Environment(EnvType.CLIENT)
	public static class Client implements ClientModInitializer {
		public static FabricKeyBinding keyCrawl;
		
		@Override
		public void onInitializeClient() {
			keyCrawl =
				FabricKeyBinding.Builder.create(
					new Identifier("crawl:crawl"),
					InputUtil.Type.KEYSYM,
					GLFW.GLFW_KEY_B,
					"key.categories.movement"
				).build();
			KeyBindingRegistryImpl.INSTANCE.register(keyCrawl);
		}
		
		static float func(double rad) {
			rad = rad % (Math.PI * 2);
			if(rad <= Math.PI / 2.0)
				return (float) Math.cos(rad*2.0);
			return (float)(
				-Math.cos((rad - Math.PI / 2.0) * (2.0 / 3.0))
			);
		}
		
		// That's bad for comp. with other mods, temp. solution..
		public static <E extends LivingEntity> void postTransformModel(BipedEntityModel<E> model, LivingEntity e, float dist) {
			PlayerEntity player = (PlayerEntity)e;
			
			MinecraftClient mc = MinecraftClient.getInstance();
			
			if((player == mc.player && mc.options.perspective == 0) || e.getPose() != Shared.CRAWLING) {
				tryRestorePlayerModel(model);
				return;
			}
			
			float yOffset = 20;
			float as = 1F;

			model.head.setRotationPoint(0, yOffset, -6);
			model.headwear.setRotationPoint(0, yOffset, -6);
			
			model.body.setRotationPoint(0, yOffset, -6);
			model.body.pitch = (float) (Math.PI / 2);
			model.body.yaw = (float) -(Math.sin(dist * as)) / 10F;
			model.body.roll = (float) -(Math.sin(dist * as)) / 5F;
			
			model.leftLeg.setRotationPoint(
				1.9F + -(float)Math.sin(dist * as),
				yOffset + 0.2F,
				6 + (float) -(Math.sin(dist * as) + 1)*2
			);
			model.leftLeg.pitch = (float) (Math.PI / 2);
			model.leftLeg.yaw = (float) (Math.cos(dist * as) + .7F) / 3F;
			
			model.rightLeg.setRotationPoint(
				-1.9F + -(float)Math.sin(dist * as),
				yOffset + 0.2F,
				6 + (float) -(Math.cos(dist * as) + 1)*2
			);
			model.rightLeg.pitch = (float) (Math.PI / 2);
			model.rightLeg.yaw = (float) (Math.sin(dist * as) - .7F) / 3F;
			
			model.leftArm.setRotationPoint(
				5,
				2 + yOffset,
				-4 + -2 + (float) Math.cos(dist * as)*3
			);

			model.rightArm.setRotationPoint(
				-5,
				2 + yOffset,
				-4 + -2 + (float) Math.sin(dist*as)*3
			);
			
			if(player.isUsingItem())
				return;
			
			if(model.handSwingProgress <= 0 || player.preferredHand != Hand.OFF_HAND) {
				model.leftArm.roll = (float) (-Math.PI / 2);
				model.leftArm.yaw = 0;
				model.leftArm.pitch = -1.3F + (float) func(dist * as + Math.PI / 2.0);
			}
			if(model.handSwingProgress <= 0 || player.preferredHand != Hand.MAIN_HAND) {
				model.rightArm.roll = (float) (Math.PI / 2);
				model.rightArm.yaw = 0;
				model.rightArm.pitch = -1.3F + (float) func(dist * as - Math.PI / 2.0);
			}
		}
		
		public static <E extends LivingEntity> void tryRestorePlayerModel(BipedEntityModel<E> model) {
			model.head.setRotationPoint(0, 0, 0);
			model.headwear.setRotationPoint(0, 0, 0);
			
			model.body.roll = 0;
			model.body.setRotationPoint(0, 0, 0);
			
			model.leftLeg.rotationPointX = 1.9F;
			model.rightLeg.rotationPointX = -1.9F;
			
			model.leftArm.setRotationPoint(5, 2, 0);
			model.rightArm.setRotationPoint(-5, 2, 0);
		}
	}
}
