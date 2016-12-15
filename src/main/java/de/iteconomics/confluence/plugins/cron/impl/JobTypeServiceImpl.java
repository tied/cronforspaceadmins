package de.iteconomics.confluence.plugins.cron.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

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

//	private final static Logger logger = LoggerFactory.getLogger(JobTypeServiceImpl.class);

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

		String name = request.getParameter("name");
		String url = request.getParameter("url");
		JobType jobType;
		checkNewJobTypeName(request);
		jobType = ao.create(JobType.class);
		jobType.setName(name);
		jobType.setUrl(url);

		jobType.save();
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
		String id = request.getParameter("id");
		JobType[] jobTypes = ao.find(JobType.class, Query.select().where("id = ?", id));
		if (jobTypes.length != 1)
			throw new JobTypeException("Cannot delete: job type with id" + id + " does not exist.");
		if (jobTypes.length == 1) {
			ao.delete(jobTypes[0]);
		}
	}



	@Override
	public Map<String, String> getJobTypeAttributes(JobType jobType) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Map<String, String> attributes = new HashMap<>();
		for (Method method: JobType.class.getDeclaredMethods()) {
			if (method.getReturnType().equals(String.class)) {
				attributes.put(method.getName().substring(3).toLowerCase(), (String) method.invoke(jobType));
			}

		}
		return null;
	}

	@Override
	public List<String> getJobTypeFieldNames() {
		List<String> methodNames = new ArrayList<String>();
		for (Method method: JobType.class.getDeclaredMethods()) {
			if (method.getReturnType().equals(String.class)) {
				methodNames.add(method.getName().substring(3).toLowerCase());
			}
		}
		return methodNames;
	}
}
