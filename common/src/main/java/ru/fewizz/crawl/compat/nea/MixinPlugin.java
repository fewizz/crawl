package ru.fewizz.crawl.compat.nea;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import ru.fewizz.crawl.PlatformHelperService;

public class MixinPlugin implements IMixinConfigPlugin {

    private boolean isNEALoaded = false;

    @Override public void onLoad(String mixinPackage) { isNEALoaded = PlatformHelperService.isModLoaded("notenoughanimations"); }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) { return this.isNEALoaded; }
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

}
