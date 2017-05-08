package de.iteconomics.confluence.plugins.rest;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import de.iteconomics.confluence.plugins.cron.api.JobService;
import de.iteconomics.confluence.plugins.cron.api.JobTypeService;
import de.iteconomics.confluence.plugins.cron.entities.Job;
import de.iteconomics.confluence.plugins.cron.entities.JobParameter;
import de.iteconomics.confluence.plugins.cron.entities.JobType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource of message.
 */
@Scanned
@Path("job")
public class JobInformationProvider {

	private static Logger logger = LoggerFactory.getLogger(JobInformationProvider.class);
	private JobService jobService;

	@Inject
	public void setJobTypeService(JobService jobService) {
		this.jobService = jobService;
	}

    @GET
    @Path("parameters/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public JobParameterModel[] getParameters(@PathParam("id") String id)
    {
    	Job job = jobService.getJobIfExists(id);
    	JobParameter[] jobParameters = job.getJobParameters();

    	JobParameterModel[] result = new JobParameterModel[jobParameters.length];
    	for (int i=0; i<jobParameters.length; i++) {
    		JobParameter parameter = jobParameters[i];
    		result[i] = new JobParameterModel(parameter.getName(), parameter.getValue());
    	}

    	return result;
    }

}