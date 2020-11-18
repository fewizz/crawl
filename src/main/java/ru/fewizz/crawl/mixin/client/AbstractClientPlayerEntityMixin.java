package ru.fewizz.crawl.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import ru.fewizz.crawl.PrevPoseInfo;

@Mixin(AbstractClientPlayerEntity.class)
abstract class AbstractClientPlayerEntityMixin extends PlayerEntity implements PrevPoseInfo {
    EntityPose crawl$prevPose;

    public AbstractClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Override
    public void setPose(EntityPose pose) {
        if(pose != getPose())
            crawl$prevPose = getPose();
        super.setPose(pose);
    }

    @Override
    public EntityPose getPrevPose() {
        return crawl$prevPose;
    }
}
