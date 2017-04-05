package fr.upem.http;


import java.awt.HeadlessException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import fr.upem.http.HttpTestServer.Data;

public class RequestBuilderTest {

	SocketChannel sc = null;
	@Before 
	public void init() throws IOException{
		sc = SocketChannel.open();

	}

	@After 
	public void free() throws IOException{
		sc.close();
	}

	@Test
	public void testRequestBuilder() {
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
		new RequestBuilder(HttpMethod.GET, sc).setResource("ressource");
	}

	@Test(expected = NullPointerException.class)
	public void testSetResourceNull() {
		new RequestBuilder(HttpMethod.POST, sc).setResource(null);
	}

	@Test
	public void testSetBody() {
		new RequestBuilder(HttpMethod.GET, sc).setBody(0, 0, "body", Charset.forName("UTF-8"));
	}

	@Test(expected = NullPointerException.class)
	public void testSetBodyNullString() {
		new RequestBuilder(HttpMethod.GET, sc).setBody(0, 0, null, Charset.forName("UTF-8"));
	}

	@Test(expected = NullPointerException.class)
	public void testSetBodyNullCharset() {
		new RequestBuilder(HttpMethod.GET, sc).setBody(0, 0, "body", null);
	}

	@Test(expected = ClosedChannelException.class)
	public void testResponseChannelClose() throws IOException{
		if(sc.isOpen()){
			sc.close();
		}
		try{
			new RequestBuilder(HttpMethod.GET, sc).setResource("ressource").response();new RequestBuilder(HttpMethod.GET, sc).setResource("ressource");
		}
		finally {
			sc = SocketChannel.open();
		}
	}


	@Test(expected = IllegalStateException.class)
	public void testResponseChannelNotConnected() throws IOException{

		try(SocketChannel channel = SocketChannel.open()){
			new RequestBuilder(HttpMethod.GET, sc).setResource("ressource").response();new RequestBuilder(HttpMethod.GET, channel).setResource("ressource");
		}

	}

	@Test
	public void testResponsePost() throws IOException {
		HttpTestServer server = HttpTestServer.create(8083);
		sc.connect(new InetSocketAddress(8083));

		String json = "{\"JobId\": \"23571113\",\"WorkerVersion\": \"1.0\",\"WorkerURL\": \"http://igm.univ-mlv.fr/~carayol/WorkerPrimeV1.jar\",\"WorkerClassName\": \"upem.workerprime.WorkerPrime\",\"Task\":        \"100\",\"ClientId\": \"Maurice\",\"Answer\" : { \"Prime\" : false, \"Facteur\" : 2}}";
		RequestBuilder builder = new RequestBuilder(HttpMethod.POST, sc)
				.setResource("Answer")
				.setBody(23571113,
						100,
						json, Charset.forName("UTF-8"));
		builder.sendRequest();
		Data data = server.post();
		assertEquals(data.header.getMethod(), "POST");
		assertEquals(data.header.getRessource(), "Answer");
		assertEquals(data.header.getContentType(), "application/json");
		assertEquals(data.bb.flip().remaining(), data.header.getContentLength());
		assertEquals(23571113, data.bb.getLong());
		assertEquals(100, data.bb.getInt());
		assertEquals(json, data.header.getCharset().decode(data.bb).toString());
		server.close();
	}
}
