package ut.de.iteconomics.confluence.plugins.rest;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import de.iteconomics.confluence.plugins.cron.api.Notifier;
import de.iteconomics.confluence.plugins.rest.JobTestRestService;
import de.iteconomics.confluence.plugins.rest.JobTestRestServiceModel;
import de.iteconomics.confluence.plugins.rest.NotificationRESTService;
import de.iteconomics.confluence.plugins.rest.NotificationModel;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.UriInfo;

public class NotificationRESTServiceTest {

	@Mock
	private Notifier notifier;
	@Mock
	private NotificationModel data;
	@Mock
	private UriInfo uriInfo;
	private NotificationRESTService underTest;

    @Before
    public void setup() {
    	MockitoAnnotations.initMocks(this);
    	configureMocks();
    	underTest = new NotificationRESTService(notifier);
    }

	private void configureMocks() {
		when(data.getTitle()).thenReturn("title");
		when(data.getMessage()).thenReturn("message");
	}

	@After
    public void tearDown() {

    }

    @Test
    public void getAnother_message_delegates_to_notifier_for_each_user() {
    	when(data.getRecipients()).thenReturn("user1" + System.getProperty("line.separator") + "user2");
    	underTest.getAnotherMessage(data, uriInfo);
    	verify(notifier).sendNotification("user1", "title", "message");
    	verify(notifier).sendNotification("user2", "title", "message");
    }

    @Test
    public void getAnother_message_delegates_to_notifier_and_removes_whitespace_from_usernames() {
    	when(data.getRecipients()).thenReturn(" user1" + System.getProperty("line.separator") + " user2 ");
    	underTest.getAnotherMessage(data, uriInfo);
    	verify(notifier).sendNotification("user1", "title", "message");
    	verify(notifier).sendNotification("user2", "title", "message");
    }

    @Test
    public void getAnother_message_delegates_to_notifier_and_ignores_empty_lines_in_recipients() {
    	when(data.getRecipients()).thenReturn("user1" + System.getProperty("line.separator") + "user2" + System.getProperty("line.separator"));
    	underTest.getAnotherMessage(data, uriInfo);
    	verify(notifier).sendNotification("user1", "title", "message");
    	verify(notifier).sendNotification("user2", "title", "message");
    }

    @Test
    public void getAnotherMessage_does_nothing_when_recipients_is_null() {
    	when(data.getRecipients()).thenReturn(null);
    	underTest.getAnotherMessage(data, uriInfo);
    	verify(notifier, times(0)).sendNotification(any(), any(), any());
    }

    @Test
    public void getAnotherMessage_turns_null_values_for_title_into_empty_string() {
    	when(data.getRecipients()).thenReturn("user");
    	when(data.getTitle()).thenReturn(null);
    	underTest.getAnotherMessage(data, uriInfo);
    	verify(notifier).sendNotification(eq("user"), eq(""), any());
    }

    @Test
    public void getAnotherMessage_turns_null_values_for_message_into_empty_string() {
    	when(data.getRecipients()).thenReturn("user");
    	when(data.getMessage()).thenReturn(null);
    	underTest.getAnotherMessage(data, uriInfo);
    	verify(notifier).sendNotification(eq("user"), any(), eq(""));
    }

}
