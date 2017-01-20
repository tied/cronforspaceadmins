package de.iteconomics.confluence.plugins.cron.impl;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.Lists;

import de.iteconomics.confluence.plugins.cron.api.JobTypeService;
import de.iteconomics.confluence.plugins.cron.entities.JobType;
import de.iteconomics.confluence.plugins.cron.exceptions.JobTypeException;
import net.java.ao.Query;


@Named
public class JobTypeServiceImpl implements JobTypeService {

	@ComponentImport
	private ActiveObjects ao;

	private static Logger logger = LoggerFactory.getLogger(JobTypeServiceImpl.class);

	@Inject
	@Override
	public void setAo(ActiveObjects ao) {
		this.ao = ao;
	}

	@Override
	public List<JobType> getAllJobTypes() {
		return Lists.newArrayList(ao.find(JobType.class));
	}

	@Override
	public void createJobType(HttpServletRequest request) {
		checkNewJobTypeName(request);
		JobType jobType = ao.create(JobType.class);
		setJobTypeValues(jobType, request);
		jobType.save();
	}

	private void setJobTypeValues(JobType jobType, HttpServletRequest request) {
		String name = request.getParameter("name");
		String url = request.getParameter("url");
		logger.error("url: " + url);
		String parameterNames = getParameterNames(url);
		logger.error("parameter names: " + parameterNames);
		String httpMethod = request.getParameter("http-method");

		jobType.setName(name);
		jobType.setHttpMethod(httpMethod);
		jobType.setParameterNames(parameterNames);
		jobType.setUrl(url);
	}

	private String getParameterNames(String url) {
		logger.error("getParameterNames called");
		String parameterNames = "";
		for (String name: getParameterNamesList(url)) {
			logger.error("param name: " + name);
			parameterNames += name;
			parameterNames += "|";
		}

		if (parameterNames.length() > 0) {
			parameterNames = parameterNames.substring(0, parameterNames.length() -1);
		}

		return parameterNames;
	}

	private List<String> getParameterNamesList(String url) {
		return getParameterNamesList(url, new ArrayList<String>());
	}

	private List<String> getParameterNamesList(String url, List<String> parameterNames) {

		logger.error("url in geParameterNamesList: " + url);

		if (url == null) {
			return parameterNames;
		}
		int nextParamStart = url.indexOf("{");
		logger.error("next param start: " + nextParamStart);
		if (nextParamStart == -1) {
			return parameterNames;
		}

		int nextParamEnd = url.indexOf("}");
		logger.error("next param end: " + nextParamEnd);

		if (nextParamEnd < nextParamStart) {
			return parameterNames;
		}

		parameterNames.add(url.substring(nextParamStart + 1, nextParamEnd));

		return getParameterNamesList(url.substring(nextParamEnd + 1), parameterNames);
	}

	private void checkNewJobTypeName(HttpServletRequest request) {
		String name = request.getParameter("name");

		JobType[] jobTypes = ao.find(JobType.class, Query.select().where("name = ?", name));

		if (jobTypes.length > 0) {
			throw new JobTypeException("Cannot create: job type with name " + name + "already exists.");
		}
	}

	@Override
	public void deleteJobType(HttpServletRequest request) {
		JobType jobType = getJobTypeIfExists(request);
		ao.delete(jobType);
	}

	private JobType getJobTypeIfExists(HttpServletRequest request) {
		String id = request.getParameter("id");
		JobType[] jobTypes = ao.find(JobType.class, Query.select().where("id = ?", id));
		if (jobTypes.length != 1) {
			throw new JobTypeException("Cannot delete: job type with id" + id + " does not exist.");
		}
		return jobTypes[0];
	}

	@Override
	public JobType getJobTypeByID(String id) {
		checkValidID(id);

		return getJobTypeIfExists(id);
	}

	private void checkValidID(String id) {
		try {
			Integer.parseInt(id);
		} catch (NumberFormatException e ) {
			throw new JobTypeException("Cannot parse job type id to int: " + id);
		}
	}

	private JobType getJobTypeIfExists(String id) {
		JobType[] matches = ao.find(JobType.class, Query.select().where("ID = ?", id));

		if (matches.length != 1) {
			throw new JobTypeException("Cannot get job type: job type with id " + id + " does not exist.");
		}

		return matches[0];
	}

	@Override
	public void updateJobType(HttpServletRequest request) {
		JobType jobType = getJobTypeIfExists(request);
		setJobTypeValues(jobType, request);
		jobType.save();
	}
}