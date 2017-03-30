package fr.upem.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;


public class RequestBuilder {
	static final Charset charsetASCII ;

	static{
		charsetASCII = Charset.forName("ASCII");
	}

	private final HttpMethod method;
	private final SocketChannel sc;
	private final Map<String, String> headers =  new HashMap<>();
	private String ressource;
	private String body;
	private Charset charset;
	private String type;
	


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

	/**
	 * Set the body of the request
	 * @param body the body of the request
	 * @return the {@link RequestBuilder}
	 * @throws NullPointerException if body is null
	 */
	public RequestBuilder appendBody(String body,String type,Charset charset){
		this.body = body;
		this.type = Objects.requireNonNull(type);
		this.charset = Objects.requireNonNull(charset);
		return this;
	}
	
	public RequestBuilder appendBody(long val){
		
		return this;
	}


	/**
	 * Get the response of the request. This method is blocking.
	 * @return the response of the request
	 * @throws IOException 
	 * @throws ClosedChannelException if the channel is closed
	 */
	public HttpResponse response() throws IOException{

		StringBuilder sb = new StringBuilder();
		sb.append(method)
		.append(" ")
		.append(ressource == null ? "/" : ressource)
		.append(" HTTP/1.1\r\n")
		.append("");
		if(body != null){
			headers.put("Content-type", "application/json; charset="+charset.name()+"\r\n");
		}
		for(Entry <String,String> entry : headers.entrySet()){
			sb.append(entry.getKey());
			sb.append(": ");
			sb.append(entry.getValue());
			sb.append("\r\n");
		}
		sb.append("\r\n");
		ByteBuffer buff = charsetASCII.encode(sb.toString());

		if(body == null){
			ByteBuffer bodyBuffer = charset.encode(body);
			buff = ByteBuffer.allocate(buff.remaining() + bodyBuffer.remaining())
					.put(buff)
					.put(bodyBuffer);
			buff.flip();
		}

		sc.write(buff);
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
			content = httpReader.readBytes(header.getContentLength());
		}
		content.flip();
		return header.getCharset().decode(content).toString();

	}



}

