package ru.fewizz.crawl.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.ToggleKeyMapping;

public class CrawlClient {
	private static final Path CONFIG_PATH = Path.of("./config/crawl.properties");
	private static final Logger LOGGER = LogManager.getLogger("CrawlClient");

	public static final OptionInstance<Boolean> crawlToggled;
	public static final KeyMapping key;
	public static boolean replaceAnimation = true;

	static {
		crawlToggled = new OptionInstance<>(
			"key.crawl",
			OptionInstance.noTooltip(),
			(optionText, value) -> value ? Options.KEY_TOGGLE : Options.KEY_HOLD,
			OptionInstance.BOOLEAN_VALUES,
			false,
			(value) -> {}
		);

		key = new ToggleKeyMapping("key.crawl", GLFW.GLFW_KEY_UNKNOWN, KeyMapping.Category.MOVEMENT, crawlToggled::get, false);

		if (Files.exists(CONFIG_PATH)) {
			Properties props = createProps();
			try {
				props.load(Files.newInputStream(CONFIG_PATH));
			} catch (IOException e) {
				LOGGER.warn("Couldn't load config", e);
			}
			replaceAnimation = Boolean.parseBoolean(props.getProperty("replace-crawl-animation"));
		}
		else {
			saveOptions();
		}
	}

	public static void saveOptions() {
		try {
			createProps().store(Files.newOutputStream(CONFIG_PATH), null);
		} catch (IOException e) {
			LOGGER.warn("Couldn't save config", e);
		}
	}

	private static Properties createProps() {
		Properties props = new Properties();
		props.setProperty("replace-crawl-animation", Boolean.toString(replaceAnimation));
		return props;
	}

}
