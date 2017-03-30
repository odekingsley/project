package fr.upem.jarset;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import fr.upem.worker.Task;

@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class JsonAnswer {
	@JsonProperty("JobId")
	private final long jobId;
	
	@JsonProperty("WorkerVersion")
	private final String workerVersion;
	
	@JsonProperty("WorkerURL")
	private final URL workerURL;
	
	@JsonProperty("WorkerClassName")
	private final String workerClassName;
	
	@JsonProperty("Task")
	private final long task;
	
	@JsonProperty("ClientId")
	private final String clientId;
	
	@JsonProperty("Answer")
	private final Answer answer;
	
	
	public JsonAnswer(Task task,String clientId,Answer answer) {
		this.clientId = clientId;
		this.answer = answer;
		jobId = task.getJobId();
		workerVersion = task.getInfo().getVersion();
		workerURL = task.getInfo().getUrl();
		workerClassName = task.getInfo().getClassName();
		this.task = task.getTask();
	}
}
