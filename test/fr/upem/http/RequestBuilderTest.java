package fr.upem.http;


import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Test;

import fr.upem.Util;
import fr.upem.http.HttpTestServer.Data;

public class RequestBuilderTest {


	@Test
	public void testRequestBuilder() throws IOException {
		SocketChannel sc = SocketChannel.open();
		new RequestBuilder(HttpMethod.POST, sc);
		new RequestBuilder(HttpMethod.GET, sc);
		sc.close();

	}
	
	
	@Test
	public void testTooLong() throws Exception {
		SocketChannel sc = SocketChannel.open();
		HttpTestServer server = HttpTestServer.create(8083);
		sc.connect(new InetSocketAddress(8083));
		StringBuilder sb = new StringBuilder();
		while(sb.toString().getBytes().length< 4096){
			sb.append("a");
		}
		Optional<HttpResponse> optional = new RequestBuilder(HttpMethod.POST, sc)
		.setBody(1, 100, sb.toString(), Util.getUtf8Charset())
		.response();
		assertFalse(optional.isPresent());
		sc.close();
		sc = SocketChannel.open();
		server.close();
		

	}
	
	@Test(expected = NullPointerException.class)
	public void testRequestBuilderNullMethod() throws IOException{
		SocketChannel sc = SocketChannel.open();
		new RequestBuilder(null, sc);
	}

	@Test(expected = NullPointerException.class)
	public void testRequestBuilderNullChannel() throws IOException{
		new RequestBuilder(HttpMethod.GET, null);
	}

	@Test
	public void testSetResource() throws IOException {
		SocketChannel sc = SocketChannel.open();
		new RequestBuilder(HttpMethod.GET, sc).setResource("ressource");
	}

	@Test(expected = NullPointerException.class)
	public void testSetResourceNull() throws IOException {
		SocketChannel sc = SocketChannel.open();
		new RequestBuilder(HttpMethod.POST, sc).setResource(null);
	}

	@Test
	public void testSetBody() throws IOException {
		SocketChannel sc = SocketChannel.open();
		new RequestBuilder(HttpMethod.GET, sc).setBody(0, 0, "body", Charset.forName("UTF-8"));
	}

	@Test(expected = NullPointerException.class)
	public void testSetBodyNullString() throws IOException {
		SocketChannel sc = SocketChannel.open();
		new RequestBuilder(HttpMethod.GET, sc).setBody(0, 0, null, Charset.forName("UTF-8"));
	}

	@Test(expected = NullPointerException.class)
	public void testSetBodyNullCharset() throws IOException {
		SocketChannel sc = SocketChannel.open();
		new RequestBuilder(HttpMethod.GET, sc).setBody(0, 0, "body", null);
	}

	@Test(expected = ClosedChannelException.class)
	public void testResponseChannelClose() throws IOException{
		SocketChannel sc = SocketChannel.open();
		sc.close();

			new RequestBuilder(HttpMethod.GET, sc).setResource("ressource").response();new RequestBuilder(HttpMethod.GET, sc).setResource("ressource");

	}


	@Test(expected = IllegalStateException.class)
	public void testResponseChannelNotConnected() throws IOException{
		
		try(SocketChannel channel = SocketChannel.open()){
			new RequestBuilder(HttpMethod.GET, channel).setResource("ressource").response();new RequestBuilder(HttpMethod.GET, channel).setResource("ressource");
		}

	}

	@Test
	public void testResponsePost() throws IOException, ExecutionException {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		HttpTestServer server = null;
		try(SocketChannel sc = SocketChannel.open()){
		
		server = HttpTestServer.create(8083);
		sc.connect(new InetSocketAddress(8083));
		Future<Data> future = executor.submit(server::serveOnce);

		String json = "{\"JobId\": \"23571113\",\"WorkerVersion\": \"1.0\",\"WorkerURL\": \"http://igm.univ-mlv.fr/~carayol/WorkerPrimeV1.jar\",\"WorkerClassName\": \"upem.workerprime.WorkerPrime\",\"Task\":        \"100\",\"ClientId\": \"Maurice\",\"Answer\" : { \"Prime\" : false, \"Facteur\" : 2}}";
		RequestBuilder builder = new RequestBuilder(HttpMethod.POST, sc)
				.setResource("Answer")
				.setBody(23571113,100,json, Util.getUtf8Charset());
		Future<Optional<HttpResponse>> future2 = executor.submit(builder::response);
		
		Data data = future.get();
		assertEquals(data.header.getMethod(), "POST");
		assertEquals(data.header.getRessource(), "Answer");
		assertEquals(data.header.getContentType(), "application/json");
		assertEquals(data.bb.flip().remaining(), data.header.getContentLength());
		assertEquals(23571113, data.bb.getLong());
		assertEquals(100, data.bb.getInt());
		assertEquals(json, data.header.getCharset().decode(data.bb).toString());

		
		Optional<HttpResponse> optional = future2.get();
		assertTrue(optional.isPresent());
		assertEquals(200, optional.get().getHeader().getCode());
		
		} catch (InterruptedException e) {
			fail("interruptedExption");
		} catch (ExecutionException e) {
			throw e;
		}finally {
			server.close();
		}
		
	}
	
	
}
