package de.iteconomics.confluence.plugins.cron.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.activeobjects.external.ActiveObjects;

import de.iteconomics.confluence.plugins.cron.entities.Job;
import de.iteconomics.confluence.plugins.cron.entities.JobType;
import de.iteconomics.confluence.plugins.cron.impl.JobTypeServiceImpl;
import de.iteconomics.confluence.plugins.cron.exceptions.JobTypeException;

@RunWith(MockitoJUnitRunner.class)
public class JobTypeServiceTest {

	private JobTypeService underTest;
	@Mock
	private ActiveObjects ao;
	@Mock
	private JobType jobType1;
	@Mock
	private HttpServletRequest request;
	@Mock
	private JobService jobService;

	public JobTypeServiceTest() {
	}

	@Before
	public void setup() {
		underTest = new JobTypeServiceImpl();
	}

	@Test
	public void getAllJobTypesShouldDelegateToAo() {
		when(ao.find(JobType.class)).thenReturn(new JobType[0]);
		underTest.setAo(ao);

		underTest.getAllJobTypes();
		verify(ao).find(JobType.class);
	}

	@Test
	public void shouldCreateJobIfNameIsNew() {
		when(ao.find(eq(JobType.class), any())).thenReturn(new JobType[0]);
		when(ao.create(JobType.class)).thenReturn(jobType1);
		when(request.getParameter("name")).thenReturn("jobType1");
		when(request.getParameter("url")).thenReturn("someUrl");
		when(request.getParameter("http-method")).thenReturn("GET");
		underTest.setAo(ao);

		underTest.createJobType(request);

		verify(ao).create(JobType.class);
		verify(jobType1).setName("jobType1");
		verify(jobType1).save();
	}

	@Test(expected=JobTypeException.class)
	public void shouldThrowExceptionWhenCreatingJobWithExistingName() {
		when(ao.find(eq(JobType.class), any())).thenReturn(new JobType[1]);
		when(ao.create(JobType.class)).thenReturn(jobType1);
		when(request.getParameter("name")).thenReturn("jobType1");
		when(request.getParameter("url")).thenReturn("someUrl");
		when(request.getParameter("http-method")).thenReturn("GET");
		underTest.setAo(ao);

		underTest.createJobType(request);
	}

	@Test
	public void shouldDeleteJobTypeIfExists() {
		when(ao.find(eq(JobType.class), any())).thenReturn(new JobType[] {jobType1});
		when(request.getParameter("name")).thenReturn("jobType1");
		when(jobService.getJobsByJobTypeID(anyInt())).thenReturn(new ArrayList<Job>());
		underTest.setAo(ao);
		underTest.setJobService(jobService);

		underTest.deleteJobType(request);

		verify(ao).delete(jobType1);
	}

	@Test(expected=JobTypeException.class)
	public void shouldThrowExceptionWhenJobToDeleteDoesNotExist() {
		when(ao.find(eq(JobType.class), any())).thenReturn(new JobType[0]);
		when(request.getParameter("name")).thenReturn("jobType1");
		underTest.setAo(ao);

		underTest.deleteJobType(request);
	}
}
