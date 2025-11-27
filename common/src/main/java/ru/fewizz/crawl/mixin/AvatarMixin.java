package ru.fewizz.crawl.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableMap;

import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import ru.fewizz.crawl.Crawl;

@Mixin(Avatar.class)
public class AvatarMixin {

    @Shadow @Final @Mutable private static Map<Pose, EntityDimensions> POSES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
	private static void onPoseMapCreation(CallbackInfo ci) {
		POSES = ImmutableMap.<Pose, EntityDimensions>builder()
			.putAll(POSES)
			.put(Crawl.Shared.CRAWLING, Crawl.Shared.CRAWLING_DIMENSIONS)
			.build();
	}

}
