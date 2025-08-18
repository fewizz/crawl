package ru.fewizz.crawl;

public interface PlayerExtended {
	boolean crawl_wasPreviouslyCrawling();
	boolean crawl_getRequestedCrawling();
	void crawl_setRequestedCrawling(boolean value);
}
