package fr.upem.http;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

public class HttpRequestHeaderTest {

	@Test
	public void testCreate() throws HttpException {
		HttpRequestHeader.create("POST Answer HTTP/1.1", new HashMap<>());
	}
	
	@Test(expected=HttpException.class)
	public void testCreateWrongMethod() throws HttpException{
		HttpRequestHeader.create("GETT Answer HTTP/1.1", new HashMap<>());

	}
	
	@Test(expected=HttpException.class)
	public void testCreateWrongVersion() throws HttpException{
		HttpRequestHeader.create("GET Answer HTTP/-1.1", new HashMap<>());

	}
	
	@Test(expected=HttpException.class)
	public void testCreateBadlyFormed() throws HttpException{
		HttpRequestHeader.create("GET HTTP/1.1", new HashMap<>());

	}
	

	@Test
	public void testGetRessource() throws HttpException {
		HttpRequestHeader header = HttpRequestHeader.create("GET Answer HTTP/1.1", new HashMap<>());
		assertEquals("Answer", header.getRessource());
	}

	@Test
	public void testGetMethod() throws HttpException {
		HttpRequestHeader header = HttpRequestHeader.create("POST Answer HTTP/1.1", new HashMap<>());
		assertEquals("POST", header.getMethod());
	}

	@Test
	public void testGetRequest() throws HttpException {
		HttpRequestHeader header = HttpRequestHeader.create("POST Answer HTTP/1.1", new HashMap<>());
		assertEquals("POST Answer HTTP/1.1", header.getRequest());
	}

	@Test
	public void testGetVersion() throws HttpException {
		HttpRequestHeader header = HttpRequestHeader.create("POST Answer HTTP/1.1", new HashMap<>());
		assertEquals("HTTP/1.1", header.getVersion());
	}

}
