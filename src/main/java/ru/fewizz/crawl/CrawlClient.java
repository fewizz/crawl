package ru.fewizz.crawl;

import java.util.Objects;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.ToggleKeyMapping;

public class CrawlClient implements ClientModInitializer {

	public static KeyMapping key;
	public static OptionInstance<Boolean> crawlToggled;

	@Override
	public void onInitializeClient() {
		CrawlClient.crawlToggled = new OptionInstance<>("key.crawl", OptionInstance.noTooltip(), (optionText, value) -> {
			return (Boolean)value ? Options.MOVEMENT_TOGGLE : Options.MOVEMENT_HOLD;
		}, OptionInstance.BOOLEAN_VALUES, false, (value) -> {});
		Objects.requireNonNull(CrawlClient.crawlToggled);

		key = new ToggleKeyMapping("key.crawl", GLFW.GLFW_KEY_UNKNOWN, KeyMapping.CATEGORY_MOVEMENT, crawlToggled::get);
		KeyBindingHelper.registerKeyBinding(key);
	}

}
