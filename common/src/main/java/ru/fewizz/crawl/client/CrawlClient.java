package ru.fewizz.crawl.client;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.ToggleKeyMapping;

public class CrawlClient {

	public static final OptionInstance<Boolean> crawlToggled;
	public static final KeyMapping key;
	public static boolean replaceAnimation = true;

	static {
		crawlToggled = new OptionInstance<>(
			"key.crawl",
			OptionInstance.noTooltip(),
			(optionText, value) -> value ? Options.MOVEMENT_TOGGLE : Options.MOVEMENT_HOLD,
			OptionInstance.BOOLEAN_VALUES,
			false,
			(value) -> {}
		);

		key = new ToggleKeyMapping("key.crawl", GLFW.GLFW_KEY_UNKNOWN, KeyMapping.CATEGORY_MOVEMENT, crawlToggled::get);
	}

	public static void readOptions() {
	}

	public static void saveOptions() {
	}

}
