package de.iteconomics.confluence.plugins.rest;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import de.iteconomics.confluence.plugins.cron.api.JobTypeService;
import de.iteconomics.confluence.plugins.cron.entities.JobType;
import de.iteconomics.confluence.plugins.cron.entities.JobTypeParameter;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource of message.
 */
@Scanned
@Path("jobtype")
public class JobTypeInformationProvider {

	private static Logger logger = LoggerFactory.getLogger(JobTypeInformationProvider.class);
	private JobTypeService jobTypeService;

	public void setJobTypeService(JobTypeService jobTypeService) {
		this.jobTypeService = jobTypeService;
	}

    @GET
    @Path("parameters/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public JobTypeParameterModel[] getParameters(@PathParam("id") String id)
    {
    	JobType jobType = jobTypeService.getJobTypeByID(id);
    	JobTypeParameter[] jobTypeParameters = jobType.getParameters();

    	List<JobTypeParameterModel> parameters = new ArrayList<>();
    	for (JobTypeParameter jobTypeParameter: jobTypeParameters) {
    		parameters.add(new JobTypeParameterModel(jobTypeParameter.getName(), jobTypeParameter.getFriendlyName(), jobTypeParameter.getDescription(), jobTypeParameter.isPathParameter()));
    	}

    	return parameters.toArray(new JobTypeParameterModel[parameters.size()]);
    }

    @GET
    @Path("description/{id}")
    @Produces({MediaType.TEXT_PLAIN})
    public String getDescription(@PathParam("id") String id)
    {
    	return jobTypeService.getJobTypeByID(id).getDescription();
    }

    @GET
    @Path("authentication/{id}")
    @Produces({MediaType.TEXT_PLAIN})
    public String isAuthenticationRequired(@PathParam("id") String id)
    {
    	JobType jobType = jobTypeService.getJobTypeByID(id);
    	return Boolean.toString(jobType.isAuthenticationRequired());
    }

}