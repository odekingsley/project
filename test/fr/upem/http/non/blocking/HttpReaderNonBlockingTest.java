package fr.upem.http.non.blocking;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.upem.Util;
import fr.upem.http.HttpException;
import fr.upem.http.HttpRequestHeader;

public class HttpReaderNonBlockingTest {
	private static  int PORT = 8080;
	private final Charset ASCII_CHARSET = Util.getAsciiCharset();
	private Thread serverThread;
	private FakeServer fakeServer;
	
	
	@Before
	public void init() throws IOException{
		fakeServer = new FakeServer(PORT);
		serverThread = new Thread(() -> {
			try {
				fakeServer.launch();
			} catch (IOException e) {
				return;
			}
		});
		serverThread.start();
	}
	
	@After
	public void close() throws IOException{
		serverThread.interrupt();;
		fakeServer.close();
		
	}
	@Test
	public void testReadHeader() throws IOException, InterruptedException {
		
		
		SocketChannel sc = SocketChannel.open(new InetSocketAddress("localhost", PORT));
		sc.write(ASCII_CHARSET.encode("GET Task HTTP/1.1\r\n\r\n"));
		try{
			HttpRequestHeader header = fakeServer.getRequest().get().getHeader();
			assertEquals(header.getMethod(), "GET");
			assertEquals("Task", header.getRessource());
		}catch (UncheckedIOException e) {
			throw e.getCause();
		}
		sc.close();
		
	}
	
	@Test(expected = HttpException.class)
	public void testReadHeaderNoMethod() throws IOException, InterruptedException {
		
		
		SocketChannel sc = SocketChannel.open(new InetSocketAddress("localhost", PORT));
		sc.write(ASCII_CHARSET.encode("Task HTTP/1.1\r\n\r\n"));
		try{
			fakeServer.getRequest().get().getHeader();
		}catch (UncheckedIOException e) {
			throw e.getCause();
		}
		sc.close();
		
	}
	
	@Test(expected = HttpException.class)
	public void testReadHeaderNoRessource() throws IOException, InterruptedException {
		
		SocketChannel sc = SocketChannel.open(new InetSocketAddress("localhost", PORT));
		sc.write(ASCII_CHARSET.encode("GET HTTP/1.1\r\n\r\n"));
		try{
			fakeServer.getRequest().get().getHeader();
		}catch (UncheckedIOException e) {
			throw e.getCause();
		}
		sc.close();
		
	}
	
	@Test
	public void testReadHeaderWithAttribute() throws IOException, InterruptedException {
		
		SocketChannel sc = SocketChannel.open(new InetSocketAddress("localhost", PORT));
		sc.write(ASCII_CHARSET.encode("POST Answer HTTP/1.1\r\nContent-Type: application/json; charset=utf-8\r\n\r\n"));
		try{
			HttpRequestHeader header = fakeServer.getRequest().get().getHeader();
			assertEquals(header.getMethod(), "POST");
			assertEquals("Answer", header.getRessource());
			assertEquals("application/json", header.getContentType());
			assertEquals("utf-8", header.getCharset().name().toLowerCase());
		}catch (UncheckedIOException e) {
			throw e.getCause();
		}
		sc.close();
		
	}

	@Test(expected=HttpException.class)
	public void testReadHeaderMalformedAttributes() throws IOException, InterruptedException {
		
		SocketChannel sc = SocketChannel.open(new InetSocketAddress("localhost", PORT));
		sc.write(ASCII_CHARSET.encode("POST Answer HTTP/1.1\r\nContent-Type\r\n\r\n"));
		try{
			fakeServer.getRequest().get().getHeader();
		}catch (UncheckedIOException e) {
			throw e.getCause();
		}
		sc.close();
		
	}
	
	@Test
	public void testReadRequestBody() throws IOException, InterruptedException{
		SocketChannel sc = SocketChannel.open(new InetSocketAddress("localhost", PORT));
		String string = "hello";
		ByteBuffer buff = ASCII_CHARSET.encode(string);
		int contentLength = buff.remaining();
		sc.write(ASCII_CHARSET.encode("POST Answer HTTP/1.1\r\nContent-Type: application/json; charset="+ASCII_CHARSET.name()+"\r\nContent-Length:"+contentLength+"\r\n\r\n"));
		sc.write(buff);
		try{
			HttpRequest request = fakeServer.getRequest().get();
			HttpRequestHeader header = request.getHeader();
			assertEquals(header.getMethod(), "POST");
			assertEquals("Answer", header.getRessource());
			assertEquals("application/json", header.getContentType());
			assertEquals(ASCII_CHARSET.name(), header.getCharset().name());
			assertEquals(contentLength, header.getContentLength());
			assertEquals(string, ASCII_CHARSET.decode(request.getBody().flip()).toString());
		}catch (UncheckedIOException e) {
			throw e.getCause();
		}
		
		
		sc.close();
	}
	
	@Test
	public void testReadRequestMultiple() throws IOException, InterruptedException{
		System.out.println("Multiple");
		
		SocketChannel sc = SocketChannel.open(new InetSocketAddress("localhost", PORT));
		
		sc.write(ASCII_CHARSET.encode("GET Task HTTP/1.1\r\n\r\n"));
		try{
			HttpRequestHeader header = fakeServer.getRequest().get().getHeader();
			assertEquals(header.getMethod(), "GET");
			assertEquals("Task", header.getRessource());
		}catch (UncheckedIOException e) {
			throw e.getCause();
		}
		
		
		String string = "hello";
		ByteBuffer buff = ASCII_CHARSET.encode(string);
		int contentLength = buff.remaining();
		sc.write(ASCII_CHARSET.encode("POST Answer HTTP/1.1\r\nContent-Type: application/json; charset="+ASCII_CHARSET.name()+"\r\nContent-Length:"+contentLength+"\r\n\r\n"));
		sc.write(buff);
		try{
			HttpRequest request = fakeServer.getRequest().get();
			HttpRequestHeader header = request.getHeader();
			assertEquals(header.getMethod(), "POST");
			assertEquals("Answer", header.getRessource());
			assertEquals("application/json", header.getContentType());
			assertEquals(ASCII_CHARSET.name(), header.getCharset().name());
			assertEquals(contentLength, header.getContentLength());
			assertEquals(string, ASCII_CHARSET.decode(request.getBody().flip()).toString());
		}catch (UncheckedIOException e) {
			throw e.getCause();
		}
		
		
		sc.close();
	}
}
