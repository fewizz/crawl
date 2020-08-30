package ru.fewizz.crawl;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import com.chocohead.mm.api.ClassTinkerers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class CrawlMod implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger();
    public static final Identifier CRAWL_IDENTIFIER = new Identifier("crawl:identifier");
    public static boolean animationOnly = false;
    
	@Override
	public void onInitialize() {
		File config = new File(FabricLoader.getInstance().getConfigDirectory(), "crawl.properties");
		Properties properties = new Properties();

		if (config.exists()) {
			try (FileInputStream stream = new FileInputStream(config)) {
				properties.load(stream);
			} catch (IOException e) {
				LOGGER.warn("Could not read property file '" + config.getAbsolutePath() + "'", e);
			}
		}

		animationOnly = Boolean.parseBoolean((String)properties.computeIfAbsent("animation_only", str -> "false"));

		if(!animationOnly)
			registerListener();

		try (FileOutputStream stream = new FileOutputStream(config)) {
			properties.store(stream, "Applied only on (dedicated/integrated) server side");
		} catch (IOException e) {
			LOGGER.warn("Could not store property file '" + config.getAbsolutePath() + "'", e);
		}
	}
	
	void registerListener() {
		ServerSidePacketRegistry.INSTANCE.register(CRAWL_IDENTIFIER, (context, buf) -> {
			boolean val = buf.readBoolean();
			context.getTaskQueue().execute(() -> context.getPlayer().getDataTracker().set(Shared.CRAWLING_REQUEST, val));
		});
	}
	
	public static class Shared {
		public static final EntityPose CRAWLING = ClassTinkerers.getEnum(EntityPose.class, EntityPoseHack.CRAWLING);
		public static final EntityDimensions CRAWLING_DIMENSIONS = new EntityDimensions(0.6F, 0.6F, false);
		public static final TrackedData<Boolean> CRAWLING_REQUEST = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	}
	
	@Environment(EnvType.CLIENT)
	public static class Client implements ClientModInitializer {
		public static KeyBinding keyCrawl;
		
		@Override
		public void onInitializeClient() {
			keyCrawl = new KeyBinding (
				"key.crawl",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_B,
				"key.categories.movement"
			);
			if(!animationOnly)
				KeyBindingHelper.registerKeyBinding(keyCrawl);
		}
		
		static float someFunc(double rad) {
			rad = rad % (Math.PI * 2);
			if(rad <= Math.PI / 2.0)
				return (float) Math.cos(rad*2.0);
			return (float)(
				-Math.cos((rad - Math.PI / 2.0) * (2.0 / 3.0))
			);
		}
		
		// That's bad for comp. with other mods, temp. solution..
		public static <E extends LivingEntity> void postTransformModel(BipedEntityModel<E> model, LivingEntity e, float dist) {
			MinecraftClient mc = MinecraftClient.getInstance();
			
			if((mc.player == e && mc.options.getPerspective().isFirstPerson())
				|| e.getPose() != Shared.CRAWLING) {
				tryRestorePlayerModel(model);
				return;
			}
			
			float yOffset = 20;
			float as = 1F;
			float pitchMul = e.getLeaningPitch(mc.getTickDelta());

			model.head.setPivot(0, yOffset*pitchMul, -6*pitchMul);
			model.helmet.setPivot(0, yOffset*pitchMul, -6*pitchMul);
			
			model.torso.setPivot(0, yOffset*pitchMul, -6*pitchMul);
			model.torso.pitch = (float) (Math.PI / 2)*pitchMul;
			model.torso.yaw = (float) -(Math.sin(dist * as))*pitchMul / 10F;
			model.torso.roll = (float) -(Math.sin(dist * as))*pitchMul / 5F;
			
			model.leftLeg.setPivot(
				1.9F + -(float) Math.sin(dist * as)*pitchMul,
				MathHelper.lerp(pitchMul, 12, yOffset + 0.2F),
				(6.5F + (float) -(Math.sin(dist * as) + 1)*2)*pitchMul
			);
			model.leftLeg.pitch = (float) (Math.PI / 2)*pitchMul;
			model.leftLeg.yaw = MathHelper.lerp(pitchMul, model.leftLeg.yaw, (float) (Math.cos(dist * as) + .7F) / 3F);
			
			model.rightLeg.setPivot(
				-1.9F + -(float) Math.sin(dist * as)*pitchMul,
				MathHelper.lerp(pitchMul, 12, yOffset + 0.2F),
				(6.5F + (float) -(Math.cos(dist * as) + 1)*2)*pitchMul
			);
			model.rightLeg.pitch = (float) (Math.PI / 2)*pitchMul;
			model.rightLeg.yaw = MathHelper.lerp(pitchMul, model.rightLeg.yaw, (float) (Math.sin(dist * as) - .7F) / 3F);

			float xArmOffset = 4.2F;
			model.leftArm.setPivot(
				xArmOffset,
				2 + yOffset*pitchMul,
				(-6 + (float) Math.cos(dist*as)*2)*pitchMul
			);

			model.rightArm.setPivot(
				-xArmOffset,
				2 + yOffset*pitchMul,
				(-6 + (float) Math.sin(dist*as)*2)*pitchMul
			);
			
			if(e.isUsingItem()) {
				return;
			}
			
			if(model.handSwingProgress <= 0 || e.preferredHand != Hand.OFF_HAND) {
				model.leftArm.roll = MathHelper.lerp(pitchMul, model.leftArm.roll, (float) (-Math.PI / 2));
				model.leftArm.yaw = 0;
				model.leftArm.pitch = MathHelper.lerp(pitchMul, model.leftArm.pitch, -1.3F + someFunc(dist * as + Math.PI / 2.0));
			}
			if(model.handSwingProgress <= 0 || e.preferredHand != Hand.MAIN_HAND) {
				model.rightArm.roll = MathHelper.lerp(pitchMul, model.rightArm.roll, (float) (Math.PI / 2));
				model.rightArm.yaw = 0;
				model.rightArm.pitch = MathHelper.lerp(pitchMul, model.rightArm.pitch, -1.3F + someFunc(dist * as - Math.PI / 2.0));
			}
		}
		
		static <E extends LivingEntity> void tryRestorePlayerModel(BipedEntityModel<E> model) {
			model.head.setPivot(0, 0, 0);
			//model.head.pitch = 0;
			model.head.roll = 0;
			model.helmet.setPivot(0, 0, 0);
			
			model.torso.roll = 0;
			model.torso.setPivot(0, 0, 0);
			
			model.leftLeg.pivotX = 1.9F;
			model.rightLeg.pivotX = -1.9F;
			
			model.leftArm.setPivot(5, 2, 0);
			model.rightArm.setPivot(-5, 2, 0);
		}
	}
}
