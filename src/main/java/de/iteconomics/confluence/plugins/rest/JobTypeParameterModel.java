package de.iteconomics.confluence.plugins.rest;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JobTypeParameterModel {

	@XmlElement
	private String name;
	@XmlElement
	private String friendlyName;
	@XmlElement
	private String description;
	@XmlElement
	private boolean isPathParameter;

	public JobTypeParameterModel(String name, String friendlyName, String description, boolean isPathParameter) {
		this.name = name;
		this.friendlyName = friendlyName;
		this.description = description;
		this.isPathParameter = isPathParameter;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	public boolean isPathParameter() {
		return isPathParameter;
	}

	public void setPathParameter(boolean isPathParameter) {
		this.isPathParameter = isPathParameter;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
