package fr.upem.worker;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * 
 * @author ode
 */
@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class Task {

	private final long jobId;
	private final String version;
	private URL workerUrl;
	private final String workerClassName;
	private final int task;

	@JsonCreator
	public Task(HashMap<String,Object> map) throws MalformedURLException {
		jobId = Long.parseLong(map.get("JobId").toString());
		task = Integer.parseInt(map.get("Task").toString());
		version = (String)map.get("WorkerVersion");
		workerUrl = new URL(map.get("WorkerURL").toString());
		workerClassName = (String)map.get("WorkerClassName");
	}

	public Task(long jobId, int task,String version, URL workerUrl, String workerClassName) {
		this.jobId = jobId;
		this.version = version;
		this.workerUrl = workerUrl;
		this.workerClassName = workerClassName;
		this.task = task;
	}

	public int getTask() {
		return task;
	}

	public long getJobId() {
		return jobId;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (jobId ^ (jobId >>> 32));
		result = prime * result + (int) (task ^ (task >>> 32));
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		result = prime * result + ((workerClassName == null) ? 0 : workerClassName.hashCode());
		result = prime * result + ((workerUrl == null) ? 0 : workerUrl.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Task other = (Task) obj;
		if (jobId != other.jobId)
			return false;
		if (task != other.task)
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		if (workerClassName == null) {
			if (other.workerClassName != null)
				return false;
		} else if (!workerClassName.equals(other.workerClassName))
			return false;
		if (workerUrl == null) {
			if (other.workerUrl != null)
				return false;
		} else if (!workerUrl.equals(other.workerUrl))
			return false;
		return true;
	}

	public String getVersion() {
		return version;
	}

	public URL getWorkerUrl() {
		return workerUrl;
	}

	public String getWorkerClassName() {
		return workerClassName;
	}

}

