package ru.fewizz.crawl.mixininterface;

import net.minecraft.entity.EntityPose;

public interface PrevPoseState {
	EntityPose getPrevPose();
	EntityPose getPrevTickPose();
}
