package de.iteconomics.confluence.plugins.cron.entities;

import net.java.ao.Entity;
import net.java.ao.Preload;

@Preload
public interface JobType extends Entity {

	String getName();
	String getUrl();

	void setName(String name);
	void setUrl(String url);
}
