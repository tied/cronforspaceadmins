package de.iteconomics.confluence.plugins.cron.entities;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.Preload;
import net.java.ao.schema.Table;

@Preload
@Table("JOB_TYPE")
public interface JobType extends Entity {

	String getName();
	String getDescription();
	String getHttpMethod();
	String getUrl();
	@OneToMany()
	JobTypeParameter[] getParameters();
	String getUsername();
	String getPassword();
	String getBundledJobTypeID();
	boolean isAuthenticationRequired();

	void setName(String name);
	void setDescription(String description);
	void setHttpMethod(String httpMethod);
	void setUrl(String url);
	void setUsername(String username);
	void setPassword(String password);
	void setBundledJobTypeID(String id);
	void setAuthenticationRequired(boolean authenticationRequired);
}
