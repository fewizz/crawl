package ru.fewizz.theotherside.launch;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import ru.fewizz.theotherside.Mod;

import java.lang.reflect.Field;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class HackPlugin implements IMixinConfigPlugin {
    public HackPlugin() {
        try {
            // IM CRYING
            LaunchClassLoader lcl = (LaunchClassLoader) HackPlugin.class.getClassLoader();
            Field transformersField = LaunchClassLoader.class.getDeclaredField("transformers");
            transformersField.setAccessible(true);
            List<IClassTransformer> transformers = (List<IClassTransformer>)transformersField.get(lcl);

            Field modsCountField = AbstractList.class.getDeclaredField("modCount");
            modsCountField.setAccessible(true);
            int modCount = modsCountField.getInt(transformers);
            Mod.onPreInitialize();
            modsCountField.setInt(transformers, modCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
