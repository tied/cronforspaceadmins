package de.iteconomics.confluence.plugins.cron.entities;

import net.java.ao.Entity;
import net.java.ao.Preload;

@Preload
public interface JobType extends Entity {

	String getName();
	String getHttpMethod();
	String getUrl();
	String getParameterNames();
	boolean isAuthenticationRequired();

	void setName(String name);
	void setHttpMethod(String httpMethod);
	void setUrl(String url);
	void setParameterNames(String parameterNames);
	void setAuthenticationRequired(boolean authenticationRequired);
}
