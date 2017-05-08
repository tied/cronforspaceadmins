package de.iteconomics.confluence.plugins.cron.entities;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.Table;

@Preload
@Table("JOB_PARAMETER")
public interface JobParameter extends Entity {

	String getName();
	String getValue();
	boolean isPathParameter();
	Job getJob();

	void setName(String name);
	void setValue(String value);
	void setJob(Job job);
	void setPathParameter(boolean isPathParameter);
}

