package ru.fewizz.crawl;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class CrawlClient implements ClientModInitializer {

    public static KeyBinding key;

    @Override
    public void onInitializeClient() {
        key = new KeyBinding("key.crawl", GLFW.GLFW_KEY_C, "key.categories.movement");
        KeyBindingHelper.registerKeyBinding(key);

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            var keySaveToolbarActivator = client.options.keySaveToolbarActivator;

            if(keySaveToolbarActivator.boundKey.equals(key.boundKey) && key.boundKey.getCode() == GLFW.GLFW_KEY_C) {
                keySaveToolbarActivator.boundKey = InputUtil.Type.KEYSYM.createFromCode(GLFW.GLFW_KEY_UNKNOWN);
                KeyBinding.updateKeysByCode();
            }
        });
    }
}
