package ru.fewizz.crawl.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public class OptionsScreen extends OptionsSubScreen {

	public OptionsScreen(Screen screen) {
		super(
			screen,
			Minecraft.getInstance().options,
			Component.literal("Crawl options")
		);
	}

	@Override
	protected void addOptions() {
		
	}
	
}
