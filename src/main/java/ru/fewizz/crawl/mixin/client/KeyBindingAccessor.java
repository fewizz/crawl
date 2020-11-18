package ru.fewizz.crawl.mixin.client;

import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {
    @Accessor(value = "boundKey")
    InputUtil.Key getBoundKey();

    @Accessor(value = "defaultKey")
    void setDefaultKey(InputUtil.Key k);


}
