package ru.fewizz.crawl;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.AbstractClientPlayerEntity;
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

public class Crawl implements ModInitializer {
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
}
