package fr.upem.Http;

import java.nio.channels.SocketChannel;
import java.util.Objects;

public class RequestBuilder {
	
	
	private final HttpMethod method;
	private final SocketChannel sc;

	
	/**
	 * Construct a new RequestBuilder
	 * @param method the method of the request
	 * @param sc the SocketChannel, the socketChannel has to be connected
	 * @throws IllegalArgumentException if the SocketChannel is not connected
	 */
	public RequestBuilder(HttpMethod method, SocketChannel sc) {
		if(! sc.isConnected()){ //implicit null check
			throw new IllegalArgumentException();
		}
		this.method = Objects.requireNonNull(method);
		this.sc = sc;
	}
	
	/**
	 * set the resource of the request
	 * @param string the resource
	 */
	public void setResource(String string){
		// TODO
	}
	
	public void setBody(String body){
		// TODO
	}
	
	public HttpResponse response(){
		//TODO
		return null;
	}
	
}
