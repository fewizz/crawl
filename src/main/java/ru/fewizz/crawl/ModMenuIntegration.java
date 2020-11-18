package ru.fewizz.crawl;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setSavingRunnable(CrawlClient::saveConfig);

            ConfigCategory options = builder.getOrCreateCategory(new LiteralText("Crawl Config"));

            options.addEntry(
                builder.entryBuilder()
                    .startBooleanToggle(new TranslatableText("crawlConfig.animationOnly"), CrawlClient.isAnimationOnly())
                    .setDefaultValue(false)
                    .setSaveConsumer(CrawlClient::setAnimationOnly)
                    .build()
            );

            options.addEntry(
                builder.entryBuilder()
                    .startEnumSelector(new TranslatableText("crawlConfig.keyActivationType"), CrawlClient.KeyActivationType.class, CrawlClient.getKeyActivationType())
                    .setDefaultValue(CrawlClient.KeyActivationType.HOLD)
                    .setSaveConsumer(CrawlClient::setKeyActivationType)
                    .setEnumNameProvider(e -> new TranslatableText(((CrawlClient.KeyActivationType)e).translationKey))
                    .build()
            );

            return builder.build();
        };
    }
}
