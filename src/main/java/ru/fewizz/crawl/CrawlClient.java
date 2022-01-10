package ru.fewizz.crawl;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.CyclingOption;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Option;
import net.minecraft.client.option.StickyKeyBinding;
import org.lwjgl.glfw.GLFW;

public class CrawlClient implements ClientModInitializer {

	public static KeyBinding key;
	public static boolean crawlToggled = false;

	public static final CyclingOption<Boolean> CRAWL_TOGGLED = CyclingOption.create(
		"key.crawl",
		Option.TOGGLE_TEXT,
		Option.HOLD_TEXT,
		gameOptions -> crawlToggled,
		(gameOptions, option, crawlToggled) -> CrawlClient.crawlToggled = crawlToggled
	);

	@Override
	public void onInitializeClient() {
		key = new StickyKeyBinding("key.crawl", GLFW.GLFW_KEY_UNKNOWN, KeyBinding.MOVEMENT_CATEGORY, () -> crawlToggled);
		KeyBindingHelper.registerKeyBinding(key);
	}
}
