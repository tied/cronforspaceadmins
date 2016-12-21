package ut.de.iteconomics.confluence.plugins.rest;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import de.iteconomics.confluence.plugins.rest.JobTestRestService;
import de.iteconomics.confluence.plugins.rest.JobTestRestServiceModel;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericEntity;

public class JobTestRestServiceTest {

    @Before
    public void setup() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void messageIsValid() {
        JobTestRestService resource = new JobTestRestService();

        Response response = resource.getMessage();
        final JobTestRestServiceModel message = (JobTestRestServiceModel) response.getEntity();

        assertEquals("wrong message","Hello World",message.getMessage());
    }
}
