package de.iteconomics.confluence.plugins.cron.entities;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.Preload;
import net.java.ao.schema.Table;


@Preload
@Table("JOB")
public interface Job extends Entity {

	String getName();
	String getJobTypeID();
	String getSpaceKey();
	String getCronExpression();
	String getJobKey();
	@OneToMany
	JobParameter[] getJobParameters();
	boolean isActive();
	boolean isJobTypeChanged();

	void setName(String name);
	void setJobTypeID(String ID);
	void setSpaceKey(String spaceKey);
	void setCronExpression(String cronExpression);
	void setJobKey(String jobKey);
	void setActive(boolean isActive);
	void setJobTypeChanged(boolean isJobTypeChanged);
}
