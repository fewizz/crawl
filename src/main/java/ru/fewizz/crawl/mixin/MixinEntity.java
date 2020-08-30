package ru.fewizz.crawl.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.fewizz.crawl.CrawlMod;

@Mixin(Entity.class)
public class MixinEntity {

    @Inject(method = "getJumpVelocityMultiplier", at = @At("RETURN"), cancellable = true)
    void onGetJumpVelocityMultiplierReturn(CallbackInfoReturnable<Float> cir) {
        if(( (Entity) ( (Object)this )).getPose() == CrawlMod.Shared.CRAWLING)
            cir.setReturnValue(cir.getReturnValueF()/2f);
    }
}
