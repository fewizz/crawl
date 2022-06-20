package ru.fewizz.crawl.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import ru.fewizz.crawl.PrevPoseInfo;

@Mixin(AbstractClientPlayerEntity.class)
abstract class AbstractClientPlayerEntityMixin extends PlayerEntity implements PrevPoseInfo {

	@Unique
	EntityPose prevPose;

	public AbstractClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile, PlayerPublicKey publicKey) {
		super(world, pos, yaw, profile, publicKey);
	}

	@Override
	public void setPose(EntityPose pose) {
		if(pose != getPose())
			prevPose = getPose();
		super.setPose(pose);
	}

	@Override
	public EntityPose getPrevPose() {
		return prevPose;
	}

}
