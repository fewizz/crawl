package ru.fewizz.crawl;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.ToggleKeyMapping;

public class CrawlClient {

	public static final OptionInstance<Boolean> crawlToggled;
	public static final KeyMapping key;

	static {
		crawlToggled = new OptionInstance<>("key.crawl", OptionInstance.noTooltip(), (optionText, value) -> {
			return (Boolean)value ? Options.MOVEMENT_TOGGLE : Options.MOVEMENT_HOLD;
		}, OptionInstance.BOOLEAN_VALUES, false, (value) -> {});

		key = new ToggleKeyMapping("key.crawl", GLFW.GLFW_KEY_UNKNOWN, KeyMapping.CATEGORY_MOVEMENT, crawlToggled::get);
	}

}
