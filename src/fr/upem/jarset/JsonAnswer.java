package fr.upem.jarset;

import java.net.URL;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import fr.upem.worker.Task;

@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class JsonAnswer {
	@JsonProperty("JobId")
	private final String jobId;
	
	@JsonProperty("WorkerVersion")
	private final String workerVersion;
	
	@JsonProperty("WorkerURL")
	private final String workerURL;
	
	@JsonProperty("WorkerClassName")
	private final String workerClassName;
	
	@JsonProperty("Task")
	private final String task;
	
	@JsonProperty("ClientId")
	private final String clientId;
	
	@JsonProperty("Answer")
	private final Map<String,Object> answer;
	
	
	public JsonAnswer(Task task,String clientId,Map<String,Object> answer) {
		this.clientId = clientId;
		this.answer = answer;
		jobId = Long.toString(task.getJobId());
		workerVersion = task.getInfo().getVersion();
		workerURL = task.getInfo().getUrl().toString();
		workerClassName = task.getInfo().getClassName();
		this.task = Long.toString(task.getTask());
	}
}
