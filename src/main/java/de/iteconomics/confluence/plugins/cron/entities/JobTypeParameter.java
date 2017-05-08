package de.iteconomics.confluence.plugins.cron.entities;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.Table;

@Preload
@Table("JOB_TYPE_PARAMETER")
public interface JobTypeParameter extends Entity {

	String getName();
	String getFriendlyName();
	String getDescription();
	JobType getJobType();
	boolean isPathParameter();

	void setJobType(JobType jobType);
	void setFriendlyName(String friendlyName);
	void setDescription(String description);
	void setName(String parameters);
	void setPathParameter(boolean isPathParameter);
}

