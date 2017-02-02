package de.iteconomics.confluence.plugins.cron.entities;

import net.java.ao.Entity;
import net.java.ao.Preload;


@Preload
public interface Job extends Entity {

	String getName();
	String getJobTypeID();
	String getSpaceKey();
	String getCronExpression();
	String getJobKey();
	String getParameters();
	String getUsername();
	String getPassword();

	void setName(String name);
	void setJobTypeID(String ID);
	void setSpaceKey(String spaceKey);
	void setCronExpression(String cronExpression);
	void setJobKey(String jobKey);
	void setParameters(String parameters);
	void setUsername(String username);
	void setPassword(String password);
}
