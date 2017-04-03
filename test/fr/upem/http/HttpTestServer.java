package fr.upem.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class HttpTestServer {

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
	
	public void serveOnce() throws IOException {
		SocketChannel sc = ssc.accept();
		HttpReader reader = new HttpReader(sc, bb);
		HttpHeader header = reader.readHeader();

	}
}
