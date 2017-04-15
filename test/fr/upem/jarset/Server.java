package fr.upem.jarset;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.upem.http.HttpReader;
import fr.upem.http.HttpRequestHeader;


public class Server {
	private final ServerSocketChannel ssc;
	private final ByteBuffer bb;
	private final Charset utf8 = Charset.forName("UTF-8");
	private final Charset ascii = Charset.forName("ASCII");
	private Server(ServerSocketChannel ssc,ByteBuffer bb) {
		this.ssc = ssc;
		this.bb = bb;
	}

	public static Server create(int port) throws IOException{
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.bind(new InetSocketAddress(port));
		return new Server(serverSocketChannel, ByteBuffer.allocate(4096));	
	}


	public void close() throws IOException{
		ssc.close();
	}

	public String get(String task) throws IOException{
		SocketChannel sc = ssc.accept();

		HttpReader reader = new HttpReader(sc, bb);
		HttpRequestHeader header = reader.readRequestHeader();

		
		if(! header.getMethod().equals("GET")){
			return "Undefined method";
		}
		if(! header.getRessource().equals("Task")){
			return "Undefined ressource";
		}
		
		ByteBuffer buff = utf8.encode(task);
		String headerString = "HTTP/1.1 200 OK\r\nContent-Type: application/json; charset=utf-8\r\nContent-Length: "+buff.remaining()+"\r\n\r\n";

		sc.write(ascii.encode(headerString));
		sc.write(buff);
		return null;
	}

	public String manageTask(String error) throws IOException{
		SocketChannel sc = ssc.accept();
		
		HttpReader reader = new HttpReader(sc, bb);
		HttpRequestHeader header = reader.readRequestHeader();

		
		if(! header.getMethod().equals("POST")){
			sc.write(ascii.encode("HTTP/1.1 400 Bad Request\r\n\r\n"));
			return "Undefined method";
		}
		if(! header.getRessource().equals("Answer")){
			sc.write(ascii.encode("HTTP/1.1 400 Bad Request\r\n\r\n"));
			return "Undefined ressource";
		}
		
		ObjectMapper mapper = new ObjectMapper();
		ByteBuffer content = reader.readBytes(header.getContentLength());
		
		content.flip();
		content.getLong();
		content.getInt();
		String string = header.getCharset().decode(content).toString();
		JsonAnswerError value = mapper.readValue(string, JsonAnswerError.class);
		String object = value.getError();
		if( ! error.equals(object)){
			sc.write(ascii.encode("HTTP/1.1 400 Bad Request\r\n\r\n"));
			return "Need error "+error+" but was "+object ;
		}
		
		sc.write(ascii.encode("HTTP/1.1 200 OK\r\n\r\n"));
		
		return null;
	}
}
