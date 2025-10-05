package ru.fewizz.crawl;

public interface PlayerExtended {
	boolean wasPreviouslyCrawling();
	boolean getRequestedCrawling();
	void setRequestedCrawling(boolean value);
}
