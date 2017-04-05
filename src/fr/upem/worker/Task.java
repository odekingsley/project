package fr.upem.worker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * 
 * @author ode
 */
@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class Task {
	
	private final long jobId;
	private final WorkerInfo info;
	private final long task;
	
	 @JsonCreator
	 public Task(HashMap<String,String> map) throws MalformedURLException {
	      jobId = Long.parseLong(map.get("JobId").toString());
	      task = Long.parseLong(map.get("Task").toString());
	      info = new WorkerInfo((String)map.get("WorkerVersion"),
	    		  new URL(map.get("WorkerURL").toString()), 
	    		  (String)map.get("WorkerClassName"));
	 }
	 
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
	
	 
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((info == null) ? 0 : info.hashCode());
		result = prime * result + (int) (jobId ^ (jobId >>> 32));
		result = prime * result + (int) (task ^ (task >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if ( !(obj instanceof Task))
			return false;
		Task other = (Task) obj;
		return info.equals(other.info) && jobId == other.jobId && task == other.task;
	}

	@Override
	public String toString() {
		return "Task [jobId=" + jobId + ", info=" + info + ", task=" + task + "]";
	}

	public static void main(String[] args) throws JsonParseException, IOException {
		String json = "{\"JobId\": \"23571113\",\"WorkerVersion\": \"1.0\",\"WorkerURL\": \"http://igm.univ-mlv.fr/~carayol/WorkerPrimeV1.jar\",\"WorkerClassName\": \"upem.workerprime.WorkerPrime\",\"Task\":\"100\"}";
		 ObjectMapper mapper = new ObjectMapper();
		 TypeFactory factory = TypeFactory.defaultInstance();
		 MapType type = factory.constructMapType(HashMap.class, String.class, String.class);
		 HashMap<String, String> map = mapper.readValue(json, type);
		 
		 Task object = mapper.readValue(json, Task.class);
		 System.out.println(object);
	}
}

