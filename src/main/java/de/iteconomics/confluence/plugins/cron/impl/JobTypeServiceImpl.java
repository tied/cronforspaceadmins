package de.iteconomics.confluence.plugins.cron.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
		String httpMethod = request.getParameter("http-method");

		jobType.setName(name);
		jobType.setHttpMethod(httpMethod);
		jobType.setUrl(url);
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