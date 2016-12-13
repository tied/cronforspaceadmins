package de.iteconomics.confluence.plugins.cron.impl;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.inject.Inject;
//import org.springframework.stereotype.Component;
import javax.inject.Named;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.Lists;

import de.iteconomics.confluence.plugins.cron.api.JobTypeService;
import de.iteconomics.confluence.plugins.cron.entities.JobType;
import net.java.ao.Query;


@Named
public class JobTypeServiceImpl implements JobTypeService {

	@ComponentImport
	private ActiveObjects ao;

	@Inject
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
		JobType jobType;
		jobType = ao.create(JobType.class);
		jobType.setName(name);
		jobType.save();
	}

	@Override
	public void deleteJobType(HttpServletRequest request) {
		String name = request.getParameter("name");
		JobType[] jobTypes = ao.find(JobType.class, Query.select().where("name = ?", name));
		if (jobTypes.length == 1) {
			ao.delete(jobTypes[0]);
		}
	}


}
