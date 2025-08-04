package ru.fewizz.crawl.fabric.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.minecraft.client.gui.screens.Screen;
import ru.fewizz.crawl.client.OptionsScreen;

public class ModMenuEntry implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return (Screen parent) -> new OptionsScreen(parent);
	}

}
