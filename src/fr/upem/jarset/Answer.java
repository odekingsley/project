package fr.upem.jarset;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class Answer {
	@JsonProperty("Prime")
	private boolean prime;
	
	@JsonProperty("Facteur")
	private int facteur;
	
	
}
