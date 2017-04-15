package fr.upem.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import fr.upem.Util;

public class HttpTestServer {

	static class Data{
		ByteBuffer bb;
		HttpRequestHeader header;
	}
	private final ServerSocketChannel ssc;
	private final ByteBuffer bb;

	private HttpTestServer(ServerSocketChannel ssc,ByteBuffer bb) {
		this.ssc = ssc;
		this.bb = bb;
	}
	
	public static HttpTestServer create(int port) throws IOException{
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.bind(new InetSocketAddress(port));
		return new HttpTestServer(serverSocketChannel, ByteBuffer.allocate(4096));
		
	}
	
	public Data serveOnce() throws IOException {
		bb.clear();
		SocketChannel sc = ssc.accept();
		HttpReader reader = new HttpReader(sc, bb);
		Data data = new Data();
		data.header = reader.readRequestHeader();
		int contentLength = data.header.getContentLength();
		data.bb = reader.readBytes(contentLength);
		sc.write(Util.getAsciiCharset().encode("HTTP/1.1 200 OK\r\n\r\n"));
		return data;
		
	}
	

	public void close() throws IOException {
		ssc.close();
		
	}
}
