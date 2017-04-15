package fr.upem.jarset;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.upem.worker.Task;
/**
 * This class represent an Answer
 * @author ode
 *
 */
abstract class Answer {

	@JsonProperty("JobId")
	protected final String jobId;
	@JsonProperty("WorkerVersion")
	protected final String workerVersion;
	@JsonProperty("WorkerURL")
	protected final String workerURL;
	@JsonProperty("WorkerClassName")
	protected final String workerClassName;
	@JsonProperty("Task")
	protected final String task;
	@JsonProperty("ClientId")
	protected final String clientId;

	public Answer(Task task,String clientId) {
		this.clientId = clientId;
		jobId = Long.toString(task.getJobId());
		workerVersion = task.getVersion();
		workerURL = task.getWorkerUrl().toString();
		workerClassName = task.getWorkerClassName();
		this.task = Long.toString(task.getTask());
	}

}