package com.octo.samples.gcm.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:applicationContext-job.xml" })
public class RunBatchTest {
	
	@Autowired
	@Qualifier("batch-gcm.mainJob")
	private Job myJob;
	
	@Autowired
	private JobLauncher myJobLauncher;
	
	@Test
	public void testLaunchBatch() throws Exception {
		myJobLauncher.run(myJob, new JobParameters());
	}

}
