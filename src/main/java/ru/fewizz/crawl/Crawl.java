package ru.fewizz.crawl;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chocohead.mm.api.ClassTinkerers;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Crawl implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger();
    public static final Identifier CRAWL_IDENTIFIER = new Identifier("crawl:identifier");
	static final Properties PROPERTIES = new Properties();

	public static boolean animationOnly() { return Boolean.parseBoolean((String) PROPERTIES.computeIfAbsent("animation_only", str -> "false")); }
	public static void setAnimationOnly(boolean value) { PROPERTIES.setProperty("animation_only", Boolean.toString(value)); }

	@Override
	public void onInitialize() {
		loadConfig();
		registerListener();
	}

	public static void loadConfig() {
		Path config = FabricLoader.getInstance().getConfigDir().resolve("crawl.properties");

		if (Files.exists(config)) {
			try (Reader reader = Files.newBufferedReader(config)) {
				PROPERTIES.load(reader);
			} catch (IOException e) {
				LOGGER.warn("Could not read property file '" + config + "'", e);
			}
		}
	}

	public static void saveConfig() {
		Path config = FabricLoader.getInstance().getConfigDir().resolve("crawl.properties");

		try (Writer writer = Files.newBufferedWriter(config)) {
			PROPERTIES.store(writer, "");
		} catch (IOException e) {
			LOGGER.warn("Could not store property file '" + config + "'", e);
		}
	}
	
	void registerListener() {
		ServerSidePacketRegistry.INSTANCE.register(CRAWL_IDENTIFIER, (context, buf) -> {
			boolean val = buf.readBoolean();
			if(animationOnly()) return;
			context.getTaskQueue().execute(() -> context.getPlayer().getDataTracker().set(Shared.CRAWLING_REQUEST, val));
		});
	}
	
	public static class Shared {
		public static final EntityPose CRAWLING = ClassTinkerers.getEnum(EntityPose.class, EntityPoseHack.CRAWLING);
		public static final EntityDimensions CRAWLING_DIMENSIONS = new EntityDimensions(0.6F, 0.6F, false);
		public static final TrackedData<Boolean> CRAWLING_REQUEST = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	}
}
