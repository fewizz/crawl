package ru.fewizz.crawl;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import ru.fewizz.crawl.mixin.client.GameOptionsAccessor;
import ru.fewizz.crawl.mixin.client.KeyBindingAccessor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class CrawlClient implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static KeyBinding crawlKey;
    static final Properties PROPERTIES = new Properties();

    @Override
    public void onInitializeClient() {
        loadConfig();

        crawlKey = new KeyBinding (
            "key.crawl",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories.movement"
        );

        KeyBindingHelper.registerKeyBinding(crawlKey);

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            if(isAnimationOnly())
                hideKey();
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            if(isAnimationOnly())
                restoreKey();
        });
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("crawl.properties");
    }

    public static void loadConfig() {
        if(!Files.exists(configPath())) return;

        try (Reader reader = Files.newBufferedReader(configPath())) {
            PROPERTIES.load(reader);
        } catch (IOException e) {
            LOGGER.warn("Could not read property file '" + configPath() + "'", e);
        }
    }

    public static void saveConfig() {
        try (Writer writer = Files.newBufferedWriter(configPath())) {
            PROPERTIES.store(writer, "");
        } catch (IOException e) {
            LOGGER.warn("Could not store property file '" + configPath() + "'", e);
        }
    }

    private static void hideKey() {
        GameOptions opts = MinecraftClient.getInstance().options;

        if(!ArrayUtils.contains(opts.keysAll, crawlKey)) return; // Already hidden

        ((KeyBindingAccessor)crawlKey).setDefaultKey(((KeyBindingAccessor)crawlKey).getBoundKey());
        crawlKey.setBoundKey(InputUtil.UNKNOWN_KEY);
        KeyBinding.updateKeysByCode();

        ((GameOptionsAccessor)opts).setAllKeyBindings(ArrayUtils.removeElement(opts.keysAll, crawlKey));
    }

    private static void restoreKey() {
        GameOptions opts = MinecraftClient.getInstance().options;
        if(ArrayUtils.contains(opts.keysAll, crawlKey)) return; // Already restored

        crawlKey.setBoundKey(crawlKey.getDefaultKey());
        KeyBinding.updateKeysByCode();

        ((GameOptionsAccessor)opts).setAllKeyBindings(ArrayUtils.add(opts.keysAll, crawlKey));
    }

    public static boolean isAnimationOnly() {
        return Boolean.parseBoolean((String) PROPERTIES.computeIfAbsent("animation_only", str -> "false"));
    }

    public static void setAnimationOnly(boolean value) {
        if(value) hideKey();
        else restoreKey();

        PROPERTIES.setProperty("animation_only", Boolean.toString(value));
    }

    public enum KeyActivationType {
        TOGGLE("crawlConfig.keyActivationTypeToggle"), HOLD("crawlConfig.keyActivationTypeHold");

        KeyActivationType(String translationKey) {
            this.translationKey = translationKey;
        }

        public final String translationKey;
    }

    public static KeyActivationType getKeyActivationType() {
        return KeyActivationType.valueOf((String) PROPERTIES.computeIfAbsent("key_activation_type", str -> KeyActivationType.HOLD.name()));
    }

    public static void setKeyActivationType(KeyActivationType value) {
        PROPERTIES.setProperty("key_activation_type", value.name());
    }
}
