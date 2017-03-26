package fr.upem.worker;

public class Task {
	
	private final long jobId;
	private final WorkerInfo info;
	private final long task;

	public Task(long jobId, WorkerInfo info, long task) {
		this.jobId = jobId;
		this.info = info;
		this.task = task;
	}

	public long getTask() {
		return task;
	}

	public WorkerInfo getInfo() {
		return info;
	}

	public long getJobId() {
		return jobId;
	}
}
