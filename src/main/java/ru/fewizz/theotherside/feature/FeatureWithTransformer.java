package ru.fewizz.theotherside.feature;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;
import java.util.List;

public abstract class FeatureWithTransformer extends Feature implements Opcodes, IClassTransformer {

    public FeatureWithTransformer() {
        try {
            LaunchClassLoader lcl = (LaunchClassLoader) this.getClass().getClassLoader();
            Field transformersField = LaunchClassLoader.class.getDeclaredField("transformers");
            transformersField.setAccessible(true);
            ((List<IClassTransformer>)transformersField.get(lcl)).add(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
