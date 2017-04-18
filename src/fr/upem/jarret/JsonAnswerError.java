package fr.upem.jarret;

import java.net.MalformedURLException;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import fr.upem.worker.Task;


/**
 * This class represent an json answer with an error.
 * @author ode
 *
 */
@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class JsonAnswerError extends Answer {
	@JsonProperty("Error")
	private final String error;
	
	
	public JsonAnswerError(Task task,String clientId,String error) {
		super(task,clientId);
		this.error = error;
		
	}
	
	@JsonCreator
	 public JsonAnswerError(HashMap<String,Object> map) throws MalformedURLException  {
	     super(new Task(map),map.get("ClientId").toString());
	     error = map.get("Error").toString();
	 }



	public String getError() {
		return error;
	}
	
	
}
