package de.iteconomics.confluence.plugins.cron.entities;

import net.java.ao.Entity;
import net.java.ao.Preload;


@Preload
public interface Job extends Entity {

	String getName();
	String getJobTypeID();
	String getCronExpression();

	void setName(String name);
	void setJobTypeID(String ID);
	void setCronExpression(String cronExpression);
}
