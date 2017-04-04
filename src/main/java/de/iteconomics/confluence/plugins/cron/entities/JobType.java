package de.iteconomics.confluence.plugins.cron.entities;

import net.java.ao.Entity;
import net.java.ao.Preload;

@Preload
public interface JobType extends Entity {

	String getName();
	String getHttpMethod();
	String getUrl();
	String getParameterNames();
	String getUsername();
	String getPassword();
	boolean isAuthenticationRequired();

	void setName(String name);
	void setHttpMethod(String httpMethod);
	void setUrl(String url);
	void setParameterNames(String parameterNames);
	void setUsername(String username);
	void setPassword(String password);
	void setAuthenticationRequired(boolean authenticationRequired);
}
