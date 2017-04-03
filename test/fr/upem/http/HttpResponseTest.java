package fr.upem.http;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

public class HttpResponseTest {

	@Test(expected = NullPointerException.class)
	public void testHttpResponseNullHeader() {
		new HttpResponse(null, "body");
	}
	
	public void testHttpResponseNullBody() {
		new HttpResponse(null, "body");
	}
	
	public void testHttpResponse() throws HttpException {
		new HttpResponse(HttpResponseHeader.create("HTTP/1.1", new HashMap<String,String>()), "body");
	}

	@Test
	public void testGetHeader() throws HttpException {
		HttpHeader header = HttpResponseHeader.create("HTTP/1.1 200 OK", new HashMap<String,String>());
		HttpResponse httpResponse = new HttpResponse(header, "body");
		assertEquals(header, httpResponse.getHeader());
		
	}

	@Test
	public void testGetBody() throws HttpException {
		HttpResponse httpResponse = new HttpResponse(HttpResponseHeader.create("HTTP/1.1 200 OK", new HashMap<String,String>()), "body");
		assertEquals("body", httpResponse.getBody());
	}

}
