package fr.upem.http;


public class HttpResponse {

	private final String body;
	private final HttpHeader header;

	/**
	 * Construct a new HttpResponse
	 * @param sc the channel who is already connected
	 */
	HttpResponse(HttpHeader header, String body) {
		this.header = header;
		this.body = body;
	}
	
	/**
	 * Return the header of the response
	 * @return the header of the response
	 */
	public HttpHeader getHeader(){
		
		return header;
	}
	
	
	/**
	 * Return the body of the response 
	 * @return the response
	 */
	public String getBody(){
		
		return body;
	}
}
