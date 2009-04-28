package org.fedorahosted.flies.shotoku;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.fedorahosted.flies.core.model.Repository;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

@Scope(ScopeType.APPLICATION)
@Name("shotokuUpdateService")
public class ShotokuUpdateService {

	@Logger
	Log log;
	
	ExecutorService executorService;

	public boolean isActive(){
		return true;
	}
	
	public int getPoolSize(){
		return 4;
	}
	
    @Observer("Flies.startup")
	public void start(){
		log.info("starting update service...");
		executorService = Executors.newCachedThreadPool();
		log.info("...started");

		addSomeJobs();
	}
	
	int existingjobs = 0;
	public void addSomeJobs(){
		for(int i=1;i<=10;i++){
			existingjobs++;
			UpdateTask task = new UpdateTask("task#"+existingjobs, log);
			executorService.submit(task);
			Future<String> future = executorService.submit(task, "hello world");
		}
	}
	
	@Destroy
	public void stop(){
		log.info("stopping update service...");
		executorService.shutdown();
		try {
			executorService.awaitTermination(30l, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			log.error("update service shutdown was interrupted", e);
		}
		log.info("stopped...");
	}
	
}

class UpdateTask implements Runnable{
	
	private String taskId;
	
	public UpdateTask(String taskId, Log log) {
		this.taskId = taskId;
		this.log = log;
	}
	
	Log log;
	
	@Override
	public void run() {
		log.info("Task with id {0} starting", getTaskId());
		long start = System.currentTimeMillis();
		while(true){
			long now = System.currentTimeMillis();
			if(now-start > 1000) break;
			if(now-start % 100 < 10)
				log.info("Task with id {0} has been working for {1} ms", getTaskId(), now-start);
		}
		log.info("Task with id {0} completed", getTaskId());
	}
	
	public String getTaskId(){
		return taskId;
	}
}