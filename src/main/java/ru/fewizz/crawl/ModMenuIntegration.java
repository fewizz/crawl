package ru.fewizz.crawl;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.text.LiteralText;
import org.apache.commons.lang3.StringUtils;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setSavingRunnable(Crawl::saveConfig);

            ConfigCategory options = builder.getOrCreateCategory(new LiteralText("Options"));

            options.addEntry(
                builder.entryBuilder()
                    .startBooleanToggle(new LiteralText("Animation Only"), Crawl.animationOnly())
                    .setDefaultValue(false)
                    .setSaveConsumer(Crawl::setAnimationOnly)
                    .build()
            );

            options.addEntry(
                builder.entryBuilder()
                    .startEnumSelector(new LiteralText("Key Activation Type"), CrawlClient.KeyActivationType.class, CrawlClient.keyActivationType())
                    .setDefaultValue(CrawlClient.KeyActivationType.HOLD)
                    .setSaveConsumer(CrawlClient::setKeyActivationType)
                    .setEnumNameProvider(e -> new LiteralText(StringUtils.capitalize(e.name().toLowerCase())))
                    .build()
            );

            return builder.build();
        };
    }
}
