package de.iteconomics.confluence.plugins.rest;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import de.iteconomics.confluence.plugins.cron.api.JobTypeService;
import de.iteconomics.confluence.plugins.cron.entities.JobType;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource of message.
 */
@Scanned
@Path("info")
public class JobTypeInformationProvider {

	private static Logger logger = LoggerFactory.getLogger(JobTypeInformationProvider.class);
	private JobTypeService jobTypeService;

	public void setJobTypeService(JobTypeService jobTypeService) {
		this.jobTypeService = jobTypeService;
	}

    @GET
    @AnonymousAllowed
    @Path("parameters/{id}")
    @Produces({MediaType.TEXT_PLAIN})
    public String getParameters(@PathParam("id") String id)
    {
    	JobType jobType = jobTypeService.getJobTypeByID(id);
    	return jobType.getParameterNames();
    }

    @GET
    @AnonymousAllowed
    @Path("authentication/{id}")
    @Produces({MediaType.TEXT_PLAIN})
    public String isAuthenticationRequired(@PathParam("id") String id)
    {
    	JobType jobType = jobTypeService.getJobTypeByID(id);
    	return Boolean.toString(jobType.isAuthenticationRequired());
    }

}