package ru.fewizz.crawl;

import net.minecraft.world.entity.Pose;

public interface PlayerExtended {
	Pose crawl_getPrevPose();
	boolean crawl_getRequestedCrawling();
	void crawl_setRequestedCrawling(boolean value);
}
