package ru.fewizz.crawl;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.StickyKeyBinding;
import org.lwjgl.glfw.GLFW;

import static net.minecraft.client.option.GameOptions.HOLD_KEY_TEXT;
import static net.minecraft.client.option.GameOptions.TOGGLE_KEY_TEXT;

import java.util.Objects;

public class CrawlClient implements ClientModInitializer {

	public static KeyBinding key;
	public static SimpleOption<Boolean> crawlToggled;

	@Override
	public void onInitializeClient() {
		CrawlClient.crawlToggled = new SimpleOption<>("key.crawl", SimpleOption.emptyTooltip(), (optionText, value) -> {
			return (Boolean)value ? TOGGLE_KEY_TEXT : HOLD_KEY_TEXT;
		}, SimpleOption.BOOLEAN, false, (value) -> {});
		Objects.requireNonNull(CrawlClient.crawlToggled);

		key = new StickyKeyBinding("key.crawl", GLFW.GLFW_KEY_UNKNOWN, KeyBinding.MOVEMENT_CATEGORY, () -> crawlToggled.getValue());
		KeyBindingHelper.registerKeyBinding(key);
	}

}
