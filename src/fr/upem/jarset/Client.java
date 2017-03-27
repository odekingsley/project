package fr.upem.jarset;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {

	private final SocketChannel sc;
	private final String clientId;
	private final ByteBuffer buff;

	private Client(SocketChannel sc, String clientId,ByteBuffer buff) {
		this.sc = sc;
		this.clientId = clientId;
		this.buff = buff;	
	}
	
	public static Client create(InetSocketAddress serverAddress, String clientId) throws IOException {
		// TODO Auto-generated method stub
		SocketChannel sc = SocketChannel.open(serverAddress);
		return new Client(sc, clientId, ByteBuffer.allocate(1024));
	}
	
	public void requestTask(){
		
	}
	
	public void manageTask(){
		
	}
}
