package org.jboss.gatein.tools;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class DataContainerJmxMonitorJob implements Job {
	
	DataContainerJmxMonitor task;
	
	public DataContainerJmxMonitorJob() {
		task = new DataContainerJmxMonitor();
	}
	
	

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		task.run();
	}

}
