package ru.fewizz.crawl;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class CrawlClient implements ClientModInitializer {
    public static KeyBinding crawlKey;

    @Override
    public void onInitializeClient() {
        if(ru.fewizz.crawl.Crawl.animationOnly) return;

        crawlKey = new KeyBinding (
                "key.crawl",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "key.categories.movement"
        );
        KeyBindingHelper.registerKeyBinding(crawlKey);
    }
}
