package de.iteconomics.confluence.plugins.rest;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource of message.
 */
@Path("/message")
public class JobTestRestService {

	private static Logger logger = LoggerFactory.getLogger(JobTestRestService.class);

    @POST
    @AnonymousAllowed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.TEXT_PLAIN})
    @Path("yetanother/{param}/endpoint")
    public String getAnotherMessage(@PathParam("param") String param, JobTestRestServiceModel data, @Context UriInfo ui)
    {
    	logger.error("path param: " + param);
    	logger.error("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXxx");
    	logger.error("first form param: " + data.getParam1());
    	logger.error("second form param: " + data.getParam2());
    	logger.error("all query param keys and values in the context: ");
    	for (String key: ui.getQueryParameters().keySet()) {
    		logger.error("key: " + key + ", value: " + ui.getQueryParameters().getFirst(key));
    	}
    	logger.error("all path param keys and values in the context: ");
    	for (String key: ui.getPathParameters().keySet()) {
    		logger.error("key: " + key + ", value: " + ui.getPathParameters().getFirst(key));
    	}
    	return "Imma postin'";
    }

    @GET
    @AnonymousAllowed
    @Produces({MediaType.TEXT_PLAIN})
    public String postMessage(@QueryParam("param") String param)
    {
    	logger.error("######## Test Rest service was called - http method: GET, parameter: " + param);
    	return "all is well";
    }
}