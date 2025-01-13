package ru.fewizz.crawl.mixininterface;

import net.minecraft.world.entity.Pose;

public interface PrevPoseState {
	Pose getPrevPose();
	Pose getPrevTickPose();
}
