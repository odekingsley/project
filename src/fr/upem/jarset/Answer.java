package fr.upem.jarset;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class Answer {
	@JsonProperty("Prime")
	private final boolean prime;
	
	@JsonProperty("Facteur")
	private final int facteur;
	
	public Answer(boolean prime, int facteur) {
		this.prime = prime;
		this.facteur = facteur;
	}
	
}
