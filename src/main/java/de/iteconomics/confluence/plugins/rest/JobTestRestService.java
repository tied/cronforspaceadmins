package de.iteconomics.confluence.plugins.rest;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource of message.
 */
@Path("/message")
public class JobTestRestService {

	private static Logger logger = LoggerFactory.getLogger(JobTestRestService.class);

    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getMessage()
    {
    	logger.error("######## Test Rest service was called - http method: GET");
    	return Response.ok(new JobTestRestServiceModel("Hello World")).build();
    }

    @POST
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response postMessage()
    {
    	logger.error("######## Test Rest service was called - http method: GET");
    	return Response.ok(new JobTestRestServiceModel("Hello World")).build();
    }
}