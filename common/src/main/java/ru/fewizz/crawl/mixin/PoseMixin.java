package ru.fewizz.crawl.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.entity.Pose;

@Mixin(Pose.class)
abstract public class PoseMixin {}
