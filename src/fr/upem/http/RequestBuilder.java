package fr.upem.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;


public class RequestBuilder {
	private static final Charset charsetASCII ;

	static{
		charsetASCII = Charset.forName("ASCII");
	}

	private final HttpMethod method;
	private final SocketChannel sc;
	private final Map<String, String> headers =  new HashMap<>();
	private String ressource;
	private final ByteBuffer bodBuffer =ByteBuffer.allocate(4096);
	private String charsetName;
	private Charset charset;


	/**
	 * Construct a new RequestBuilder
	 * @param method the method of the request
	 * @param sc the SocketChannel, the socketChannel has to be connected
	 * @throws NullPointerException if the method or sc is null
	 */
	public RequestBuilder(HttpMethod method, SocketChannel sc) {
		this.method = Objects.requireNonNull(method);
		this.sc = Objects.requireNonNull(sc);

	}



	/**
	 * Add a header to the request, 
	 * @param header
	 * @param value
	 * @return
	 */
	public RequestBuilder addHeader(String header, String value){
		headers.put(header, value);
		return this;
	}

	/**
	 * Set the resource of the request
	 * @param string the resource
	 * @return the {@link RequestBuilder}
	 * @throws NullPointerException if string is null
	 */
	public RequestBuilder setResource(String string){
		ressource = string;
		return this;
	}

	
	
	public RequestBuilder setBody(long jobId,int task,String string,Charset charset){
		this.charset = charset;
		bodBuffer.clear();
		
		charsetName = charset.name();
		
		bodBuffer.putLong(jobId);
		bodBuffer.putInt(task);
		bodBuffer.put(charset.encode(string));
		return this;
	}


	/**
	 * Get the response of the request. This method is blocking.
	 * @return the response of the request
	 * @throws IOException 
	 * @throws ClosedChannelException if the channel is closed
	 */
	public HttpResponse response() throws IOException{
		bodBuffer.flip();
		StringBuilder sb = new StringBuilder();
		sb.append(method)
		.append(" ")
		.append(ressource == null ? "/" : ressource)
		.append(" HTTP/1.1\r\n")
		.append("Host: ")
		.append(((InetSocketAddress)sc.getRemoteAddress()).getHostString())
		.append("\r\n");
		
		if(bodBuffer.hasRemaining()){
			headers.put("Content-type", "application/json");
			headers.put("Content-length", Integer.toString(bodBuffer.remaining()));
		}
		for(Entry <String,String> entry : headers.entrySet()){
			sb.append(entry.getKey());
			sb.append(": ");
			sb.append(entry.getValue());
			sb.append("\r\n");
		}
		sb.append("\r\n");
		ByteBuffer buff = charsetASCII.encode(sb.toString());
		System.out.println(sb.toString());
		System.out.println("octe send for header :"+sc.write(buff));
		
		if(bodBuffer.hasRemaining()){
			//System.out.println(bodBuffer.getLong());
			//System.out.println(bodBuffer.getInt());
			System.out.println(charset.decode(bodBuffer));
			bodBuffer.flip();
			//buff = ByteBuffer.allocate(buff.remaining() + bodBuffer.remaining())
					//.put(buff)
					//.put(bodBuffer);
			System.out.println("octet send for body : "+sc.write(bodBuffer));
		}

		
		HttpReader httpReader = new HttpReader(sc, ByteBuffer.allocate(1024));
		HttpHeader header = httpReader.readHeader();
		String body = readBody(httpReader,header);
		return new HttpResponse(header, body);
	}



	private String readBody(HttpReader httpReader, HttpHeader header) throws IOException {
		ByteBuffer content = null;
		if(header.isChunkedTransfer()){
			content = httpReader.readChunks();
		}
		else{
			int contentLength = header.getContentLength();
			if(contentLength == -1){
				return "";
			}
			content = httpReader.readBytes(contentLength);
		}
		content.flip();
		return header.getCharset().decode(content).toString();

	}



}

