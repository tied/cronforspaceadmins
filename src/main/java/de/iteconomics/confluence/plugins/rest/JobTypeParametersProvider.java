package de.iteconomics.confluence.plugins.rest;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import de.iteconomics.confluence.plugins.cron.api.JobTypeService;
import de.iteconomics.confluence.plugins.cron.entities.JobType;

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource of message.
 */
@Scanned
@Path("/parameters")
public class JobTypeParametersProvider {

	private static Logger logger = LoggerFactory.getLogger(JobTypeParametersProvider.class);
	private JobTypeService jobTypeService;

	public void setJobTypeService(JobTypeService jobTypeService) {
		this.jobTypeService = jobTypeService;
	}

    @GET
    @AnonymousAllowed
    @Path("{id}")
    @Produces({MediaType.TEXT_PLAIN})
    public String getMessage(@PathParam("id") String id)
    {
    	JobType jobType = jobTypeService.getJobTypeByID(id);
    	return jobType.getParameterNames();
    }

}