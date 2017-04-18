package fr.upem.jarret;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import fr.upem.worker.Task;

/**
 * This class represent a json answer with no error
 * @author ode
 *
 */
@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class JsonAnswer extends Answer {
	@JsonProperty("Answer")
	private final Map<String,Object> answer;
	
	
	public JsonAnswer(Task task,String clientId,Map<String,Object> answer) {
		super(task,clientId);
		this.answer = answer;
		
	}


	public Map<String,Object> getAnswer() {
		return Collections.unmodifiableMap(answer);
	}
	
	
}
