package fr.upem.http;

import java.util.Objects;
import java.util.Optional;

/**
 * This class represent an Http response. 
 * It contains an HttpResponseHeader and eventually a body.
 * @author ode
 */
public class HttpResponse {

	private final String body;
	private final HttpResponseHeader header;

	/**
	 * Construct a new HttpResponse
	 * @param sc the channel who is already connected
	 * @throws NullPointerException if the header is null
	 */
	HttpResponse(HttpResponseHeader header, String body) {
		this.header = Objects.requireNonNull(header);
		this.body = body;
	}
	
	/**
	 * Return the header of the response
	 * @return the header of the response
	 */
	public HttpResponseHeader getHeader(){
		
		return header;
	}
	
	
	/**
	 * Return the body of the response 
	 * @return an optional which contains the response
	 */
	public Optional<String>	 getBody(){
		
		return Optional.ofNullable(body);
	}
}
