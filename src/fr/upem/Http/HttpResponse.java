package fr.upem.Http;

import java.nio.channels.SocketChannel;

public class HttpResponse {

	private final SocketChannel sc;

	/**
	 * Construct a new HttpResponse
	 * @param sc the channel who is already connected
	 */
	HttpResponse(SocketChannel sc) {
		this.sc = sc;
	}
	
	/**
	 * Return the header of the response
	 * @return the header of the response
	 */
	public HttpHeader getHeader(){
		
		return null;
	}
	
	
	/**
	 * Return the body of the response 
	 * @return the response
	 */
	public String getBody(){
		
		return null;
	}
}
