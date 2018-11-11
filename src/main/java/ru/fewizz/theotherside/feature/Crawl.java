package ru.fewizz.theotherside.feature;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.tracker.DataTracker;
import net.minecraft.entity.tracker.TrackedData;
import net.minecraft.entity.tracker.TrackedDataHandlerRegistry;
import ru.fewizz.theotherside.asmutil.ASM;

public class Crawl extends FeatureWithTransformer {
    @Override
    public byte[] transform(String clname, String transformedName, byte[] basicClass) {
        if(clname.equals("net.minecraft.entity.player.EntityPlayer"))
            return ASM.transform(basicClass, cv -> cv.transform("initDataTracker", mv -> mv.beforeReturn(() -> {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/player/EntityPlayer", "dataTracker" ,"Lnet/minecraft/entity/tracker/DataTracker;");
                mv.visitMethodInsn(INVOKESTATIC, Crawl.class.getName().replace(".", "/"), "addCrawlTag", "(Lnet/minecraft/entity/tracker/DataTracker;)V", false);
            })));

        return basicClass;
    }

    public static void addCrawlTag(DataTracker tracker) {
        tracker.startTracking(LoadBarrier.IS_CRAWLING, false);
    }
    static class LoadBarrier{
        static final TrackedData<Boolean> IS_CRAWLING;

        static {
            IS_CRAWLING = DataTracker.registerData(EntityPlayer.class, TrackedDataHandlerRegistry.BOOLEAN);
        }
    }

    public static boolean isPlayerCrawling(EntityPlayer player) {
        return player.getDataTracker().get(LoadBarrier.IS_CRAWLING);
    }
}
