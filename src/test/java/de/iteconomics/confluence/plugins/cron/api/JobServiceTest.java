package de.iteconomics.confluence.plugins.cron.api;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.iteconomics.confluence.plugins.cron.entities.Job;
import de.iteconomics.confluence.plugins.cron.entities.JobType;
import de.iteconomics.confluence.plugins.cron.exceptions.JobException;
import de.iteconomics.confluence.plugins.cron.impl.JobServiceImpl;

import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import net.java.ao.EntityManager;
import net.java.ao.Query;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.jdbc.DatabaseUpdater;
import net.java.ao.test.jdbc.Hsql;
import net.java.ao.test.jdbc.Jdbc;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;

@Data(JobServiceTest.JobServiceDBUpdater.class)
@Jdbc(Hsql.class)
@RunWith(ActiveObjectsJUnitRunner.class)
public class JobServiceTest {

	private JobService underTest;
	private EntityManager entityManager;
	private	ActiveObjects ao;
	private int job1Id;
	private int job2Id;
	private int job3Id;
	private int job4Id;
	@Mock
	private HttpServletRequest request;
	@Mock
	private JobTypeService jobTypeService;
	@Mock
	private JobType jobType1;
	@Mock
	private JobType jobType2;
	@Mock
	private SchedulerService schedulerService;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		ao = new TestActiveObjects(entityManager);
		underTest = new JobServiceImpl();
		underTest.setAo(ao);

		configureMocks();
		injectMocks(underTest);
		createJobs();
	}

	private void configureMocks() {
		when(jobType1.getID()).thenReturn(1);
		when(jobType1.getUrl()).thenReturn("url1");
		when(jobType2.getID()).thenReturn(2);
		when(jobType2.getUrl()).thenReturn("url2");
		when(jobTypeService.getAllJobTypes()).thenReturn(Lists.newArrayList(jobType1, jobType2));
		when(jobTypeService.getJobTypeByID("1")).thenReturn(jobType1);
		when(jobTypeService.getJobTypeByID("2")).thenReturn(jobType2);
	}

	private void injectMocks(JobService underTest) {
		underTest.setJobTypeService(jobTypeService);
		underTest.setSchedulerService(schedulerService);

	}

	private void createJobs() {
		Job job1 = ao.create(Job.class);
		job1Id = job1.getID();
		job1.setName("job1");
		job1.setJobTypeID("1");
		job1.setSpaceKey("SPACE1");
		job1.setCronExpression("1 * * * * ?");
		job1.setJobKey("jobkey1");
		job1.setParameters("key1=value1");
		job1.setActive(false);
		job1.setJobTypeChanged(true);
		job1.save();

		Job job2 = ao.create(Job.class);
		job2Id = job2.getID();
		job2.setName("job2");
		job2.setJobTypeID("2");
		job2.setSpaceKey("SPACE2");
		job2.setCronExpression("2 * * * * ?");
		job2.setJobKey("jobkey2");
		job2.setParameters("key2=value2");
		job2.setActive(false);
		job2.setJobTypeChanged(true);
		job2.save();

		Job job3 = ao.create(Job.class);
		job3Id = job3.getID();
		job3.setName("job3");
		job3.setJobTypeID("1");
		job3.setSpaceKey("SPACE1");
		job3.setCronExpression("3 * * * * ?");
		job3.setJobKey("jobkey3");
		job3.setParameters("key3=value3");
		job3.setActive(false);
		job3.setJobTypeChanged(true);
		job3.save();

		Job job4 = ao.create(Job.class);
		job4Id = job4.getID();
		job4.setName("job4");
		job4.setJobTypeID("2");
		job4.setSpaceKey("SPACE2");
		job4.setCronExpression("4 * * * * ?");
		job4.setJobKey("jobkey4");
		job4.setParameters("key4=value4");
		job4.setActive(false);
		job4.setJobTypeChanged(true);
		job4.save();
	}

	/*
	 * getAllJobs()
	 */

	@Test
	public void getAllJobs_should_return_job1_through_job4() {
		assertEquals(Lists.newArrayList(getJobById(job1Id), getJobById(job2Id), getJobById(job3Id), getJobById(job4Id)), underTest.getAllJobs());
	}

	/*
	 * createJob()
	 */

	@Test
	public void createJob_should_create_job_if_name_is_new() {
		List<Job> jobs = underTest.getAllJobs();
		assertEquals(4, jobs.size());
		when(request.getParameter("name")).thenReturn("newName");
		when(request.getParameter("job-type")).thenReturn("1");
		when(request.getParameter("spacekey")).thenReturn("SPACE1");
		when(request.getParameter("cron-expression")).thenReturn("3 * * * * ?");
		when(request.getParameterNames()).thenReturn(new Vector<String>(Sets.newHashSet("name", "job-type", "spacekey", "cron-expression")).elements());

		underTest.createJob(request);

		jobs = underTest.getAllJobs();
		assertEquals(5, jobs.size());
	}

	@Test
	public void createJob_should_create_job_if_name_is_in_use_in_other_space() {
		List<Job> jobs = underTest.getAllJobs();
		assertEquals(4, jobs.size());

		when(request.getParameter("name")).thenReturn("job1");
		when(request.getParameter("job-type")).thenReturn("1");
		when(request.getParameter("spacekey")).thenReturn("OTHERSPACE");
		when(request.getParameter("cron-expression")).thenReturn("3 * * * * ?");
		when(request.getParameterNames()).thenReturn(new Vector<String>(Sets.newHashSet("name", "job-type", "spacekey", "cron-expression")).elements());

		underTest.createJob(request);

		jobs = underTest.getAllJobs();
		assertEquals(5, jobs.size());
	}

	@Test(expected=JobException.class)
	public void createJob_should_throw_exception_if_name_is_in_use_in_the_same_space() {
		when(request.getParameter("name")).thenReturn("job1");
		when(request.getParameter("job-type")).thenReturn("1");
		when(request.getParameter("spacekey")).thenReturn("SPACE1");
		when(request.getParameter("cron-expression")).thenReturn("3 * * * * ?");
		when(request.getParameterNames()).thenReturn(new Vector<String>(Sets.newHashSet("name", "job-type", "spacekey", "cron-expression")).elements());

		underTest.createJob(request);

	}

	@Test(expected=JobException.class)
	public void createJob_should_throw_exception_if_job_type_id_does_not_exist() {
		when(request.getParameter("name")).thenReturn("job5");
		when(request.getParameter("job-type")).thenReturn("3");
		when(request.getParameter("spacekey")).thenReturn("SPACE1");
		when(request.getParameter("cron-expression")).thenReturn("3 * * * * ?");
		when(request.getParameterNames()).thenReturn(new Vector<String>(Sets.newHashSet("name", "job-type", "spacekey", "cron-expression")).elements());

		underTest.createJob(request);

	}


	/*
	 * deleteJob()
	 */

	@Test
	public void deleteJob_should_delete_job_if_it_exists() {
		assertEquals(4, underTest.getAllJobs().size());

		when(request.getParameter("id")).thenReturn(Integer.toString(job1Id));
		underTest.deleteJob(request);

		List<Job> jobs = underTest.getAllJobs();
		assertEquals(3, jobs.size());
		assertFalse(jobs.get(0).getID() == 1);
	}

	@Test(expected=JobException.class)
	public void deleteJob_should_throw_exception_if_job_does_not_exist() {
		when(request.getParameter("id")).thenReturn(getNonExistentId());

		underTest.deleteJob(request);
	}

	/*
	 * registerJob(Job)
	 */
	private String getNonExistentId() {
		int id = 0;
		Set<Integer> existingIds = new HashSet<Integer>();
		for (Job job: underTest.getAllJobs()) {
			existingIds.add(job.getID());
		}
		while (existingIds.contains(id)) {
			id++;
		}

		return Integer.toString(id);
	}

	@Test
	public void registerJob_should_delegate_to_schedulerService() throws SchedulerServiceException {
		underTest.registerJob(getJobById(job1Id));

		verify(schedulerService, times(1)).registerJobRunner(any(), any());
		verify(schedulerService, times(1)).scheduleJob(any(), any());
	}

	@Test
	public void registerJob_should_reset_flags() {
		Job job1 = getJobById(job1Id);
		assertFalse(job1.isActive());
		assertTrue(job1.isJobTypeChanged());

		underTest.registerJob(job1);

		assertTrue(job1.isActive());
		assertFalse(job1.isJobTypeChanged());
	}

	/*
	 * registerJob(HttpSertvletRequest)
	 */

	@Test
	public void registerJob_should_delegate_to_scheduler_service_if_job_exists() throws SchedulerServiceException {
		when(request.getParameter("id")).thenReturn(Integer.toString(job1Id));
		underTest.registerJob(request);

		verify(schedulerService, times(1)).registerJobRunner(any(), any());
		verify(schedulerService, times(1)).scheduleJob(any(), any());
	}

	@Test
	public void registerJob_should_reset_flags_if_job_exists() {
		Job job1 = getJobById(job1Id);
		when(request.getParameter("id")).thenReturn(Integer.toString(job1Id));

		assertFalse(job1.isActive());
		assertTrue(job1.isJobTypeChanged());

		underTest.registerJob(job1);

		assertTrue(job1.isActive());
		assertFalse(job1.isJobTypeChanged());
	}

	@Test(expected=JobException.class)
	public void registerJob_should_throw_exception_if_job_does_not_exists() throws SchedulerServiceException {
		when(request.getParameter("id")).thenReturn(getNonExistentId());
		underTest.registerJob(request);
	}

	@Test(expected=JobException.class)
	public void registerJob_should_throw_exception_if_job_id_is_invalid() throws SchedulerServiceException {
		when(request.getParameter("id")).thenReturn(getNonExistentId());
		underTest.registerJob(request);
	}

	/*
	 * registerAllJobs()
	 */

	@Test
	public void registerAllJob_should_delegate_to_schedulerService() throws SchedulerServiceException {
		underTest.registerAllJobs();

		verify(schedulerService, times(4)).registerJobRunner(any(), any());
		verify(schedulerService, times(4)).scheduleJob(any(), any());
	}

	@Test
	public void registerAllJob_should_reset_flags() {
		Job job1 = getJobById(job1Id);
		Job job2 = getJobById(job2Id);
		Job job3 = getJobById(job3Id);
		Job job4 = getJobById(job4Id);

		assertFalse(job1.isActive());
		assertTrue(job1.isJobTypeChanged());
		assertFalse(job2.isActive());
		assertTrue(job2.isJobTypeChanged());
		assertFalse(job3.isActive());
		assertTrue(job3.isJobTypeChanged());
		assertFalse(job4.isActive());
		assertTrue(job4.isJobTypeChanged());

		underTest.registerAllJobs();

		job1 = getJobById(job1Id);
		job2 = getJobById(job2Id);
		job3 = getJobById(job3Id);
		job4 = getJobById(job4Id);

		assertTrue(job1.isActive());
		assertFalse(job1.isJobTypeChanged());
		assertTrue(job2.isActive());
		assertFalse(job2.isJobTypeChanged());
		assertTrue(job3.isActive());
		assertFalse(job3.isJobTypeChanged());
		assertTrue(job4.isActive());
		assertFalse(job4.isJobTypeChanged());
	}

	/*
	 * isEnabled()
	 */

	@Test
	public void isEnabled_returns_true_for_enabled_job() {
		Job job2 = getJobById(job2Id);
		when(schedulerService.getRegisteredJobRunnerKeys()).thenReturn(Sets.newHashSet(JobRunnerKey.of(job2.getJobKey() + ":runner")));

		boolean enabled = underTest.isEnabled(job2);
		assertTrue(enabled);
	}

	@Test
	public void isEnabled_returns_false_for_disabled_job() {
		Job job2 = getJobById(job2Id);
		when(schedulerService.getRegisteredJobRunnerKeys()).thenReturn(new HashSet<JobRunnerKey>());

		boolean enabled = underTest.isEnabled(job2);
		assertFalse(enabled);
	}

	@Test
	public void isEnabled_returns_false_when_job_is_null() {
		boolean enabled = underTest.isEnabled(null);
		assertFalse(enabled);
	}

	/*
	 * getDisabledJobs()
	 */

	@Test
	public void getDisabledJobs_returns_job2_through_job4_when_only_job1_is_enabled() {
		Job job1 = getJobById(job1Id);
		Job job2 = getJobById(job2Id);
		Job job3 = getJobById(job3Id);
		Job job4 = getJobById(job4Id);
		when(schedulerService.getRegisteredJobRunnerKeys()).thenReturn(Sets.newHashSet(
				JobRunnerKey.of(job1.getJobKey() + ":runner")));

		Set<Job> disabledJobs = underTest.getDisabledJobs();

		assertEquals(3, disabledJobs.size());
		assertTrue(disabledJobs.containsAll(Lists.newArrayList(job2, job3, job4)));
	}

	@Test
	public void getDisabledJobs_returns_job3_and_job4_when_job1_and_job2_are_enabled() {
		Job job1 = getJobById(job1Id);
		Job job2 = getJobById(job2Id);
		Job job3 = getJobById(job3Id);
		Job job4 = getJobById(job4Id);
		when(schedulerService.getRegisteredJobRunnerKeys()).thenReturn(Sets.newHashSet(
				JobRunnerKey.of(job1.getJobKey() + ":runner"),
				JobRunnerKey.of(job2.getJobKey() + ":runner")));
		Set<Job> disabledJobs = underTest.getDisabledJobs();

		assertEquals(2, disabledJobs.size());
		assertTrue(disabledJobs.contains(job3));
		assertTrue(disabledJobs.contains(job4));
	}

	@Test
	public void getDisabledJobs_returns_an_empty_set_when_job1_through_job4_are_enabled() {
		Job job1 = getJobById(job1Id);
		Job job2 = getJobById(job2Id);
		Job job3 = getJobById(job3Id);
		Job job4 = getJobById(job4Id);
		when(schedulerService.getRegisteredJobRunnerKeys()).thenReturn(Sets.newHashSet(
				JobRunnerKey.of(job1.getJobKey() + ":runner"),
				JobRunnerKey.of(job2.getJobKey() + ":runner"),
				JobRunnerKey.of(job3.getJobKey() + ":runner"),
				JobRunnerKey.of(job4.getJobKey() + ":runner")));

		Set<Job> disabledJobs = underTest.getDisabledJobs();
		assertEquals(0, disabledJobs.size());
	}

	@Test
	public void getDisabledJobs_returns_job1_through_job4_when_neither_is_enabled() {
		Job job1 = getJobById(job1Id);
		Job job2 = getJobById(job2Id);
		Job job3 = getJobById(job3Id);
		Job job4 = getJobById(job4Id);
		Set<Job> disabledJobs = underTest.getDisabledJobs();

		assertEquals(4, disabledJobs.size());
		assertTrue(disabledJobs.contains(job1));
		assertTrue(disabledJobs.contains(job2));
		assertTrue(disabledJobs.contains(job3));
		assertTrue(disabledJobs.contains(job4));
	}

	/*
	 * unregisterJob(Job)
	 */

	@Test
	public void unregisterJob_should_delegate_to_schedulerService() {
		Job job2 = getJobById(job2Id);

		underTest.unregisterJob(job2);

		verify(schedulerService, times(1)).unscheduleJob(JobId.of(job2.getJobKey()));
		verify(schedulerService, times(1)).unregisterJobRunner(JobRunnerKey.of(job2.getJobKey() + ":runner"));
	}

	/*
	 * unregisterJob(HttpServletRequest)
	 */

	@Test
	public void unregisterJob_should_delegate_to_schedulerService_if_job_exists() {
		Job job2 = getJobById(job2Id);
		when(request.getParameter("id")).thenReturn(Integer.toString(job2Id));

		underTest.unregisterJob(request);

		verify(schedulerService, times(1)).unscheduleJob(JobId.of(job2.getJobKey()));
		verify(schedulerService, times(1)).unregisterJobRunner(JobRunnerKey.of(job2.getJobKey() + ":runner"));
	}

	@Test(expected=JobException.class)
	public void unregisterJob_should_throw_exception_if_job_does_not_exist() {
		when(request.getParameter("id")).thenReturn(getNonExistentId());

		underTest.unregisterJob(request);
	}

	/*
	 * updateJob()
	 */

	@Test
	public void updateJob_should_update_values_correctly() {
		when(request.getParameter("id")).thenReturn(Integer.toString(job1Id));
		when(request.getParameter("name")).thenReturn("newName");
		when(request.getParameter("job-type")).thenReturn("2");
		when(request.getParameter("cron-expression")).thenReturn("3 * * * * ?");
		when(request.getParameter("job-key")).thenReturn("newJobKey");
		when(request.getParameter("parameters")).thenReturn("newKey=newValue");
		// not changing spacekey, but a value is required
		when(request.getParameter("spacekey")).thenReturn("SPACE1");
		// to avoid NPE
		when(request.getParameterNames()).thenReturn(new Vector<String>(Sets.newHashSet("name", "job-type", "spacekey", "cron-expression")).elements());

		underTest.updateJob(request);

		Job job1 = getJobById(job1Id);
		assertEquals("newName", job1.getName());
		assertEquals("2", job1.getJobTypeID());
		assertEquals("3 * * * * ?", job1.getCronExpression());
	}

	@Test
	public void updateJob_should_NOT_throw_an_exception_if_name_is_unchanged() {
		when(request.getParameter("id")).thenReturn(Integer.toString(job1Id));
		when(request.getParameter("name")).thenReturn("job1");
		when(request.getParameter("job-type")).thenReturn("2");
		when(request.getParameter("cron-expression")).thenReturn("3 * * * * ?");
		when(request.getParameter("job-key")).thenReturn("newJobKey");
		when(request.getParameter("parameters")).thenReturn("newKey=newValue");
		// not changing spacekey, but a value is required
		when(request.getParameter("spacekey")).thenReturn("SPACE1");
		// to avoid NPE
		when(request.getParameterNames()).thenReturn(new Vector<String>(Sets.newHashSet("name", "job-type", "spacekey", "cron-expression")).elements());

		underTest.updateJob(request);
	}

	@Test
	public void updateJob_should_NOT_throw_an_exception_when_non_required_parameters_are_missing() {
		when(request.getParameter("id")).thenReturn(Integer.toString(job1Id));
		when(request.getParameter("name")).thenReturn("job1");
		when(request.getParameter("job-type")).thenReturn("2");
		when(request.getParameter("cron-expression")).thenReturn("3 * * * * ?");
		// to avoid NPE
		when(request.getParameterNames()).thenReturn(new Vector<String>(Sets.newHashSet("name", "job-type", "spacekey", "cron-expression")).elements());

		underTest.updateJob(request);
	}

	@Test(expected=JobException.class)
	public void updateJob_should_throwException_if_name_is_in_use() {
		when(request.getParameter("id")).thenReturn(Integer.toString(job1Id));
		when(request.getParameter("name")).thenReturn("job2");
		when(request.getParameter("job-type")).thenReturn("2");
		when(request.getParameter("cron-expression")).thenReturn("3 * * * * ?");
		when(request.getParameter("job-key")).thenReturn("newJobKey");
		when(request.getParameter("parameters")).thenReturn("newKey=newValue");
		// changing spacekey to create name conflict with job2
		when(request.getParameter("spacekey")).thenReturn("SPACE2");
		// to avoid NPE
		when(request.getParameterNames()).thenReturn(new Vector<String>(Sets.newHashSet("name", "job-type", "spacekey", "cron-expression")).elements());

		underTest.updateJob(request);
	}

	@Test(expected=JobException.class)
	public void updateJob_should_throwException_if_name_is_missing() {
		when(request.getParameter("id")).thenReturn(Integer.toString(job1Id));
		when(request.getParameter("job-type")).thenReturn("2");
		when(request.getParameter("cron-expression")).thenReturn("3 * * * * ?");
		when(request.getParameter("job-key")).thenReturn("newJobKey");
		when(request.getParameter("parameters")).thenReturn("newKey=newValue");
		when(request.getParameter("spacekey")).thenReturn("SPACE1");
		// to avoid NPE
		when(request.getParameterNames()).thenReturn(new Vector<String>(Sets.newHashSet("name", "job-type", "spacekey", "cron-expression")).elements());

		underTest.updateJob(request);
	}

	@Test(expected=JobException.class)
	public void updateJob_should_throwException_if_id_is_missing() {
		when(request.getParameter("name")).thenReturn("newName");
		when(request.getParameter("job-type")).thenReturn("2");
		when(request.getParameter("cron-expression")).thenReturn("3 * * * * ?");
		when(request.getParameter("job-key")).thenReturn("newJobKey");
		when(request.getParameter("parameters")).thenReturn("newKey=newValue");
		when(request.getParameter("spacekey")).thenReturn("SPACE1");
		// to avoid NPE
		when(request.getParameterNames()).thenReturn(new Vector<String>(Sets.newHashSet("name", "job-type", "spacekey", "cron-expression")).elements());

		underTest.updateJob(request);
	}

	@Test(expected=JobException.class)
	public void updateJob_should_throwException_if_job_type_is_missing() {
		when(request.getParameter("id")).thenReturn(Integer.toString(job1Id));
		when(request.getParameter("name")).thenReturn("job1");
		when(request.getParameter("cron-expression")).thenReturn("3 * * * * ?");
		when(request.getParameter("job-key")).thenReturn("newJobKey");
		when(request.getParameter("parameters")).thenReturn("newKey=newValue");
		when(request.getParameter("spacekey")).thenReturn("SPACE1");
		// to avoid NPE
		when(request.getParameterNames()).thenReturn(new Vector<String>(Sets.newHashSet("name", "job-type", "spacekey", "cron-expression")).elements());

		underTest.updateJob(request);
	}

	@Test(expected=JobException.class)
	public void updateJob_should_throwException_if_cron_expression_is_missing() {
		when(request.getParameter("id")).thenReturn(Integer.toString(job1Id));
		when(request.getParameter("name")).thenReturn("job1");
		when(request.getParameter("job-type")).thenReturn("2");
		when(request.getParameter("job-key")).thenReturn("newJobKey");
		when(request.getParameter("parameters")).thenReturn("newKey=newValue");
		when(request.getParameter("spacekey")).thenReturn("SPACE1");
		// to avoid NPE
		when(request.getParameterNames()).thenReturn(new Vector<String>(Sets.newHashSet("name", "job-type", "spacekey", "cron-expression")).elements());

		underTest.updateJob(request);
	}

	/*
	 * getJobs(String spaceKey)
	 */

	@Test
	public void getJobs_should_return_jobs_with_given_space_key() {
		Job job1 = getJobById(job1Id);
		Job job3 = getJobById(job3Id);
		Job job2 = getJobById(job2Id);
		Job job4 = getJobById(job4Id);

		List<Job> jobs = underTest.getJobs("SPACE1");
		assertEquals(2, jobs.size());
		assertTrue(jobs.contains(job1));
		assertTrue(jobs.contains(job3));

		jobs = underTest.getJobs("SPACE2");
		assertEquals(2, jobs.size());
		assertTrue(jobs.contains(job2));
		assertTrue(jobs.contains(job4));
	}

	@Test
	public void getJobs_should_return_empty_list_for_nonexistent_space_key() {
		List<Job> jobs = underTest.getJobs("OTHERSPACE");
		assertTrue(jobs.isEmpty());
	}

	/*
	 * getJobsByJobTypeId()
	 */

	@Test
	public void getJobsByJobTypeId_should_return_jobs_with_matching_job_type() {
		Job job1 = getJobById(job1Id);
		Job job3 = getJobById(job3Id);
		Job job2 = getJobById(job2Id);
		Job job4 = getJobById(job4Id);

		List<Job> jobsForJobType1 = underTest.getJobsByJobTypeID(1);
		List<Job> jobsForJobType2 = underTest.getJobsByJobTypeID(2);

		assertEquals(2, jobsForJobType1.size());
		assertEquals(2, jobsForJobType2.size());
		assertTrue(jobsForJobType1.contains(job1));
		assertTrue(jobsForJobType1.contains(job3));
		assertTrue(jobsForJobType2.contains(job2));
		assertTrue(jobsForJobType2.contains(job4));
	}

	@Test
	public void getJobsByJobTypeId_should_return_empty_list_for_nonexistent_job_type() {
		List<Job> jobs = underTest.getJobsByJobTypeID(3);

		assertTrue(jobs.isEmpty());
	}

	/*
	 * formatParameters()
	 */

	@Test
	public void formatParameters_should_replace_equals_with_colon_and_split_at_ampersand() {
		String[] formatted = underTest.formatParameters("key1=value1&key2=value2");
		assertEquals(2, formatted.length);
		assertEquals("key1: value1", formatted[0]);
		assertEquals("key2: value2", formatted[1]);
	}

	@Test
	public void formatParameters_should_return_an_empty_array_for_an_empty_string() {
		String[] formatted = underTest.formatParameters("");
		assertEquals(0, formatted.length);
	}

	@Test
	public void formatParameters_should_return_an_empty_array_for_whitespace() {
		String[] formatted = underTest.formatParameters(" ");
		assertEquals(0, formatted.length);

		formatted = underTest.formatParameters(" " + System.getProperty("line.separator"));
		assertEquals(0, formatted.length);
	}

	@Test
	public void formatParameters_should_return_an_empty_array_for_null() {
		String[] formatted = underTest.formatParameters(null);
		assertEquals(0, formatted.length);
	}


	/*
	 * non-test methods
	 */

	private Job getJobById(int id) {
		String idAsString = Integer.toString(id);
		return ao.find(Job.class, Query.select().where("id = ?", idAsString))[0];
	}

	public static final class JobServiceDBUpdater implements DatabaseUpdater {

		@Override
		public void update(EntityManager entityManager) throws Exception {
			entityManager.migrate(Job.class);
		}
	}
}
