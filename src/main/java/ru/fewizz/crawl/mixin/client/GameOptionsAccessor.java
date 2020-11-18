package ru.fewizz.crawl.mixin.client;

import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameOptions.class)
public interface GameOptionsAccessor {

    @Final
    @Mutable
    @Accessor(value = "keysAll")
    void setAllKeyBindings(KeyBinding[] kbs);
}
