package it.de.iteconomics.confluence.plugins.rest;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.mockito.Mockito;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import de.iteconomics.confluence.plugins.rest.JobTestRestService;
import de.iteconomics.confluence.plugins.rest.JobTestRestServiceModel;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;

public class JobTestRestServiceFuncTest {

    @Before
    public void setup() {

    }

    @After
    public void tearDown() {

    }

    @Test
    @Ignore
    public void messageIsValid() {

        String baseUrl = System.getProperty("baseurl");
        String resourceUrl = baseUrl + "/rest/jobtestrestservice/1.0/message";

        RestClient client = new RestClient();
        Resource resource = client.resource(resourceUrl);

        JobTestRestServiceModel message = resource.get(JobTestRestServiceModel.class);

        assertEquals("wrong message","Hello World",message.getParam1());
    }
}
