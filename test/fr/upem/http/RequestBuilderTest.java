package fr.upem.http;


import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RequestBuilderTest {
	
	SocketChannel sc = null;
	@Before 
	public void init() throws IOException{
		sc = SocketChannel.open();
		
	}
	
	@After void free() throws IOException{
		sc.close();
	}

	@Test
	public void testRequestBuilder() throws IOException {
		new RequestBuilder(HttpMethod.POST, sc);
		new RequestBuilder(HttpMethod.GET, sc);
		
	}
	@Test(expected = NullPointerException.class)
	public void testRequestBuilderNullMethod(){
		new RequestBuilder(null, sc);
	}
	
	@Test(expected = NullPointerException.class)
	public void testRequestBuilderNullChannel(){
		new RequestBuilder(HttpMethod.GET, null);
	}
	
	

	@Test
	public void testSetResource() {
		new RequestBuilder(HttpMethod.GET, null).setResource("ressource");
	}

	@Test
	public void testSetBody() {
		new RequestBuilder(HttpMethod.GET, sc).setBody("body","text/plain",Charset.forName("utf-8"));
	}

	@Test
	public void testResponse() {
		// TODO
	}

}
