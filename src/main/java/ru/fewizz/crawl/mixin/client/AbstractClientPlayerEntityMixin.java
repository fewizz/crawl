package ru.fewizz.crawl.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import ru.fewizz.crawl.Crawl;
import ru.fewizz.crawl.LastTimeCrawledInfo;

@Mixin(AbstractClientPlayerEntity.class)
abstract public class AbstractClientPlayerEntityMixin extends PlayerEntity implements LastTimeCrawledInfo {
    int crawl$crawledTicksBack;

    public AbstractClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Override
    public int crawledTicksBack() {
        return crawl$crawledTicksBack;
    }

    @Override
    public void setCrawledTicksBack(int v) {
        crawl$crawledTicksBack = v;
    }

    @Override
    public void tick() {
        boolean crawling = getPose() == Crawl.Shared.CRAWLING;
        crawl$crawledTicksBack = crawling ? 0 : crawl$crawledTicksBack + 1;
        super.tick();
    }
}
