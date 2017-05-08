package de.iteconomics.confluence.plugins.cron.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.iteconomics.confluence.plugins.cron.entities.Job;
import de.iteconomics.confluence.plugins.cron.entities.JobType;
import de.iteconomics.confluence.plugins.cron.entities.JobTypeParameter;
import de.iteconomics.confluence.plugins.cron.impl.JobTypeServiceImpl;
import net.java.ao.EntityManager;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.jdbc.DatabaseUpdater;
import net.java.ao.test.jdbc.Hsql;
import net.java.ao.test.jdbc.Jdbc;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import de.iteconomics.confluence.plugins.cron.exceptions.JobTypeException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Data(JobTypeServiceTest.JobTypeServiceDBUpdater.class)
@Jdbc(Hsql.class)
@RunWith(ActiveObjectsJUnitRunner.class)
public class JobTypeServiceTest {

	private JobTypeService underTest;
	private ActiveObjects ao;
	private JobType jobType1;
	private JobType jobType2;
	@Mock
	private HttpServletRequest request;
	@Mock
	private JobService jobService;
	private EntityManager entityManager;

	private Logger logger = LoggerFactory.getLogger(JobTypeServiceTest.class);

	public JobTypeServiceTest() {
	}

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		underTest = new JobTypeServiceImpl();
		ao = new TestActiveObjects(entityManager);
		underTest.setAo(ao);

		// required to avoid NPEs
		when(jobService.getJobsByJobTypeID(anyInt())).thenReturn(new ArrayList<Job>());
		underTest.setJobService(jobService);

		createTestJobTypes();
	}

	private void createTestJobTypes() {
		jobType1 = ao.create(JobType.class);
		jobType1.setName("jobType1");
		jobType1.setHttpMethod("GET");
		jobType1.setUrl("url1");
		jobType1.setUsername("username1");
		jobType1.setPassword("password1");
		jobType1.setBundledJobTypeID("BUNDLED1");
		jobType1.setAuthenticationRequired(true);
		jobType1.save();
		JobTypeParameter parameters = ao.create(JobTypeParameter.class);
		parameters.setName("parameters1");
		parameters.setJobType(jobType1);
		parameters.save();

		jobType2 = ao.create(JobType.class);
		jobType2.setName("jobType2");
		jobType2.setHttpMethod("GET");
		jobType2.setUrl("url2");
		jobType2.setUsername("username2");
		jobType2.setPassword("password2");
		jobType2.setBundledJobTypeID("BUNDLED2");
		jobType2.setAuthenticationRequired(true);
		jobType2.save();
		JobTypeParameter parameters2 = ao.create(JobTypeParameter.class);
		parameters2.setName("parameters2");
		parameters2.setJobType(jobType2);
		parameters2.save();

	}

	/*
	 * getAllJobTypes()
	 */

	@Test
	public void getAllJobTypes_should_return_jobType1_and_jobType2() {
		List<JobType> jobTypes= underTest.getAllJobTypes();
		assertEquals(2, jobTypes.size());
		assertThat(jobTypes, containsInAnyOrder(jobType1, jobType2));
	}

	/*
	 * createJobType()
	 */

	@Test
	public void createJobType_should_create_job_if_name_is_new() {
		List<JobType> jobTypes = underTest.getAllJobTypes();
		assertEquals(2, jobTypes.size());

		when(request.getParameter("name")).thenReturn("jobType3");
		when(request.getParameter("url")).thenReturn("someUrl");
		when(request.getParameter("http-method")).thenReturn("GET");
		// avoid NPE
		when(request.getParameterNames()).thenReturn(new Vector<String>().elements());

		underTest.createJobType(request);
		jobTypes = underTest.getAllJobTypes();
		assertEquals(3, jobTypes.size());
	}

	@Test(expected=JobTypeException.class)
	public void createJobType_should_throw_exception_when_job_name_is_already_in_use() {
		when(request.getParameter("name")).thenReturn("jobType1");
		when(request.getParameter("url")).thenReturn("someUrl");
		when(request.getParameter("http-method")).thenReturn("GET");

		underTest.createJobType(request);
	}

	/*
	 * deleteJobType
	 */

	@Test
	public void deleteJobType_should_delete_existing_job_type() {
		List<JobType> jobTypes = underTest.getAllJobTypes();
		assertEquals(2, jobTypes.size());
		when(request.getParameter("name")).thenReturn(jobType1.getName());
		when(request.getParameter("id")).thenReturn(Integer.toString(jobType1.getID()));
		when(jobService.getJobsByJobTypeID(anyInt())).thenReturn(new ArrayList<Job>());
		underTest.setJobService(jobService);

		underTest.deleteJobType(request);

		jobTypes = underTest.getAllJobTypes();
		assertEquals(1, jobTypes.size());
		assertThat(jobTypes, not(contains(jobType1)));
	}

	@Test(expected=JobTypeException.class)
	public void deleteJobType_should_throw_exception_whe_job_does_not_exist() {
		String id = getNonExistentId();
		when(request.getParameter("id")).thenReturn(id);

		underTest.deleteJobType(request);
	}

	/*
	 * getJobTypeById()
	 */

	@Test
	public void getJobTypeById_should_return_correct_job_type() {
		assertEquals(jobType1, underTest.getJobTypeByID(Integer.toString(jobType1.getID())));
		assertEquals(jobType2, underTest.getJobTypeByID(Integer.toString(jobType2.getID())));
	}

	@Test(expected=JobTypeException.class)
	public void getJobTypeById_should_throw_exception_for_nonexistent_id() {
		underTest.getJobTypeByID(getNonExistentId());
	}

	/*
	 * updateJobType()
	 */

	@Test
	public void updateJobType_should_update_fields_correctly() {
		JobType jobType = underTest.getJobTypeByID(Integer.toString(jobType1.getID()));
		assertEquals("jobType1", jobType.getName());

		when(request.getParameter("id")).thenReturn(Integer.toString(jobType.getID()));
		when(request.getParameter("name")).thenReturn("newJobTypeName");
		when(request.getParameter("http-method")).thenReturn("POST");
		when(request.getParameter("url")).thenReturn("newUrl");
		when(request.getParameter("parameter-name-1")).thenReturn("newParameterName1");
		when(request.getParameter("parameter-name-2")).thenReturn("newParameterName2");
		when(request.getParameter("username")).thenReturn("newUsername");
		when(request.getParameter("password")).thenReturn("newPassword");
		when(request.getParameter("bundled-job-type-id")).thenReturn("newBundledJobTypeId");
		when(request.getParameter("authenticaton")).thenReturn(null);
		// avoid NPE
		when(request.getParameterNames()).thenReturn(new Vector<String>(Lists.newArrayList("id", "name", "http-method", "parameter-name-1", "parameter-name-2")).elements());


		underTest.updateJobType(request);

		jobType = underTest.getJobTypeByID(Integer.toString(jobType1.getID()));
		assertEquals(jobType.getName(), "newJobTypeName");
		assertEquals(jobType.getHttpMethod(), "POST");
		assertEquals(jobType.getUrl(), "newUrl");


		JobTypeParameter[] jobTypeParameters = jobType.getParameters();
		List<String> newParameters = new ArrayList<>();
    	for (JobTypeParameter jobTypeParameter: jobTypeParameters) {
    		newParameters.add(jobTypeParameter.getName());
    	}
		assertEquals("parameters have been updated", Lists.newArrayList("newParameterName1",  "newParameterName2"), newParameters);
		assertEquals(jobType.getUsername(), "newUsername");
		assertEquals(jobType.getPassword(), "newPassword");
		assertEquals(jobType.getBundledJobTypeID(), "newBundledJobTypeId");
		assertEquals(jobType.isAuthenticationRequired(), false);
	}

	@Test(expected=JobTypeException.class)
	public void updateJobType_should_throw_exception_when_name_is_missing() {
		when(request.getParameter("id")).thenReturn(Integer.toString(jobType1.getID()));
		when(request.getParameter("http-method")).thenReturn("POST");
		when(request.getParameter("url")).thenReturn("newUrl");


		underTest.updateJobType(request);
	}

	@Test(expected=JobTypeException.class)
	public void updateJobType_should_throw_exception_when_url_is_missing() {
		when(request.getParameter("id")).thenReturn(Integer.toString(jobType1.getID()));
		when(request.getParameter("name")).thenReturn("newJobTypeName");
		when(request.getParameter("http-method")).thenReturn("POST");

		underTest.updateJobType(request);
	}

	@Test(expected=JobTypeException.class)
	public void updateJobType_should_throw_exception_when_http_method_is_missing() {


		when(request.getParameter("id")).thenReturn(Integer.toString(jobType1.getID()));
		when(request.getParameter("name")).thenReturn("newJobTypeName");
		when(request.getParameter("url")).thenReturn("newUrl");


		underTest.updateJobType(request);
	}

	@Test(expected=JobTypeException.class)
	public void updateJobType_should_throw_exception_when_id_is_missing() {
		when(request.getParameter("name")).thenReturn("newJobTypeName");
		when(request.getParameter("http-method")).thenReturn("POST");
		when(request.getParameter("url")).thenReturn("newUrl");


		underTest.updateJobType(request);
	}

	/*
	 * formatParameters()
	 */

	@Test
	public void formatParameters_should_trim_and_split_parameters_at_newlines() {
		String newline = System.getProperty("line.separator");
		String[] parameters1 = underTest.formatParameters(" one" + newline + "two" + newline + " three " + newline);
		String[] parameters2 = underTest.formatParameters("");
		String[] parameters3 = underTest.formatParameters(" ");
		String[] parameters4 = underTest.formatParameters(null);
		String[] parameters5 = underTest.formatParameters(newline);
		String[] parameters6 = underTest.formatParameters(newline + " one");
		assertArrayEquals("with extra whitespace", new String[] {"one", "two", "three"}, parameters1);
		assertArrayEquals("empty string", new String[0], parameters2);
		assertArrayEquals("space", new String[0], parameters3);
		assertArrayEquals("null", new String[0], parameters4);
		assertArrayEquals("newline", new String[0], parameters5);
		assertArrayEquals("with leading newline", new String[] {"one"}, parameters6);
	}

	/*
	 * hasNotificationJobType()
	 */

	@Test
	public void hasNotificationJobType_returns_true_when_one_notification_job_type_exists() {
		when(request.getParameter("name")).thenReturn("newJobTypeName");
		when(request.getParameter("http-method")).thenReturn("POST");
		when(request.getParameter("url")).thenReturn("newUrl");
		when(request.getParameter("bundled-job-type-id")).thenReturn("NOTIFICATION");
		when(request.getParameterNames()).thenReturn(new Vector<String>().elements());

		underTest.createJobType(request);

		assertEquals(true, underTest.hasNotificationJobType());
	}

	@Test(expected=JobTypeException.class)
	public void hasNotificationJobType_throws_exception_when_two_notification_job_types_exist() {
		when(request.getParameter("name")).thenReturn("jobType1");
		when(request.getParameter("http-method")).thenReturn("GET");
		when(request.getParameter("url")).thenReturn("url");
		when(request.getParameter("bundled-job-type-id")).thenReturn("NOTIFICATION");
		underTest.createJobType(request);

		when(request.getParameter("name")).thenReturn("jobType2");
		underTest.createJobType(request);

		underTest.hasNotificationJobType();
	}

	@Test
	public void hasNotificationJobType_returns_false_when_no_notification_job_type_exists() {
		when(request.getParameter("name")).thenReturn("jobType");
		when(request.getParameter("http-method")).thenReturn("POST");
		when(request.getParameter("url")).thenReturn("url");
		when(request.getParameter("bundled-job-type-id")).thenReturn("SOMETHING_OTHER_THAN_NOTIFICATION");
		when(request.getParameterNames()).thenReturn(new Vector<String>().elements());

		underTest.createJobType(request);

		assertEquals(false, underTest.hasNotificationJobType());
	}

	/*
	 * getNotificationJobTypeId()
	 */

	@Test
	public void getNotificationJobTypeId_returns_id_of_notification_job_type() {
		String jobType1Id = Integer.toString(jobType1.getID());

		when(request.getParameter("id")).thenReturn(jobType1Id);
		when(request.getParameter("name")).thenReturn("newJobTypeName");
		when(request.getParameter("http-method")).thenReturn("POST");
		when(request.getParameter("url")).thenReturn("newUrl");
		when(request.getParameter("bundled-job-type-id")).thenReturn("NOTIFICATION");
		// avoid NPE
		when(request.getParameterNames()).thenReturn(new Vector<String>().elements());

		underTest.updateJobType(request);

		assertEquals(jobType1Id, underTest.getNotificationJobTypeId());
	}

	@Test(expected=JobTypeException.class)
	public void getNotificationJobTypeId_throws_exception_when_two_notification_job_types_exist() {
		when(request.getParameter("name")).thenReturn("jobType1");
		when(request.getParameter("http-method")).thenReturn("GET");
		when(request.getParameter("url")).thenReturn("url");
		when(request.getParameter("bundled-job-type-id")).thenReturn("NOTIFICATION");
		underTest.createJobType(request);

		when(request.getParameter("name")).thenReturn("jobType2");
		underTest.createJobType(request);

		underTest.getNotificationJobTypeId();
	}

	@Test
	public void getNotificationJobTypeId_returns_none_when_no_notification_job_type_exists() {
		when(request.getParameter("name")).thenReturn("jobType");
		when(request.getParameter("http-method")).thenReturn("POST");
		when(request.getParameter("url")).thenReturn("url");
		when(request.getParameter("bundled-job-type-id")).thenReturn("SOMETHING_OTHER_THAN_NOTIFICATION");
		// avoid NPE
		when(request.getParameterNames()).thenReturn(new Vector<String>().elements());

		underTest.createJobType(request);

		assertEquals("none", underTest.getNotificationJobTypeId());
	}

	/*
	 * getNotificationJobTypeUsername()
	 */

	@Test
	public void getNotificationJobTypeUsername_should_return_username_of_notification_job_type() {
		String jobType1Id = Integer.toString(jobType1.getID());

		when(request.getParameter("id")).thenReturn(jobType1Id);
		when(request.getParameter("name")).thenReturn("newJobTypeName");
		when(request.getParameter("http-method")).thenReturn("POST");
		when(request.getParameter("url")).thenReturn("newUrl");
		when(request.getParameter("username")).thenReturn("newUsername");
		when(request.getParameter("bundled-job-type-id")).thenReturn("NOTIFICATION");
		// avoid NPE
		when(request.getParameterNames()).thenReturn(new Vector<String>().elements());

		underTest.updateJobType(request);

		assertEquals("newUsername", underTest.getNotificationJobTypeUsername());
	}

	@Test(expected=JobTypeException.class)
	public void getNotificationJobTypeUsername_throws_exception_when_two_notification_job_types_exist() {
		when(request.getParameter("name")).thenReturn("jobType1");
		when(request.getParameter("http-method")).thenReturn("GET");
		when(request.getParameter("url")).thenReturn("url");
		when(request.getParameter("bundled-job-type-id")).thenReturn("NOTIFICATION");
		underTest.createJobType(request);

		when(request.getParameter("name")).thenReturn("jobType2");
		underTest.createJobType(request);

		underTest.getNotificationJobTypeUsername();
	}

	@Test
	public void getNotificationJobTypeUsername_should_return_empty_string_when_no_notification_job_type_exists() {
		when(request.getParameter("name")).thenReturn("jobType");
		when(request.getParameter("http-method")).thenReturn("POST");
		when(request.getParameter("url")).thenReturn("url");
		when(request.getParameter("bundled-job-type-id")).thenReturn("SOMETHING_OTHER_THAN_NOTIFICATION");
		// avoid NPE
		when(request.getParameterNames()).thenReturn(new Vector<String>().elements());

		underTest.createJobType(request);

		assertEquals("", underTest.getNotificationJobTypeUsername());
	}

	/*
	 * non-test methods
	 */

	private String getNonExistentId() {
		int id = 0;
		Set<Integer> existingIds = new HashSet<Integer>();
		for (JobType jt: underTest.getAllJobTypes()) {
			existingIds.add(jt.getID());
		}
		while (existingIds.contains(id)) {
			id++;
		}

		return Integer.toString(id);
	}

	public static final class JobTypeServiceDBUpdater implements DatabaseUpdater {

		@Override
		public void update(EntityManager entityManager) throws Exception {
			entityManager.migrate(JobType.class, JobTypeParameter.class);
		}
	}
}
