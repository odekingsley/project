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
import java.util.Optional;

/**
 * This class build HttpRequest and send it to the receiver connected to a SocketChannel.
 * For each request, a buffer is allocated unless if a buffer is specified in the constructor.
 * @author ode
 */
public class RequestBuilder {
	private static final int BUFFER_SIZE = 4096;
	private static final int REQUEST_SIZE = 4096;
	private static final Charset charsetASCII ;
	

	static{
		charsetASCII = Charset.forName("ASCII");
	}

	private final HttpMethod method; // The method of the request
	private final SocketChannel sc; // The socket channel where to send the request
	private final Map<String, String> headers =  new HashMap<>(); // The header
	private String ressource; // The ressource of the  request
	private final ByteBuffer buffer; // The buffer to use for sending the request
	private String charsetName; // The charset of the body, may be null
	private boolean send; // If the request has been send or not
	private ByteBuffer bodyBuff; // The json body of the request, may be null 


	/**
	 * Construct a new RequestBuilder
	 * @param method the method of the request
	 * @param sc the SocketChannel
	 * @throws NullPointerException if the method or sc is null
	 */
	public RequestBuilder(HttpMethod method, SocketChannel sc) {
		this.method = Objects.requireNonNull(method);
		this.sc = Objects.requireNonNull(sc);
		buffer = ByteBuffer.allocate(BUFFER_SIZE);

	}
	
	
	/**
	 * Construct a new RequestBuilder with a specified {@link ByteBuffer} to use
	 * @param method the method of the request
	 * @param sc the SocketChannel, the socketChannel has to be connected
	 * @param buffer the byteBuffer to use
	 * @throws NullPointerException if the method or sc is null
	 */
	public RequestBuilder(HttpMethod method, SocketChannel sc, ByteBuffer buffer) {
		this.buffer = buffer;
		this.method = method;
		this.sc = Objects.requireNonNull(sc);
	}
	
	
	
	



	/**
	 * Add a header to the request. 
	 * @param header the property
	 * @param value the value
	 * @return the {@link RequestBuilder}
	 * @throws NullPointerException if header or value is null
	 */
	public RequestBuilder addHeader(String header, String value){
		headers.put(header, Objects.requireNonNull(value));// implicit null check
		return this;
	}

	/**
	 * Set the resource of the request.
	 * @param string the resource
	 * @return the {@link RequestBuilder}
	 * @throws NullPointerException if string is null
	 */
	public RequestBuilder setResource(String string){
		ressource = Objects.requireNonNull(string);
		return this;
	}

	
	/**
	 * Set the body of the request.
	 * @param jobId the id of the job
	 * @param task the number of the task
	 * @param string the json content of the task
	 * @param charset the charset of the json content
	 * @return the {@link RequestBuilder}
	 * @throws NullPointerException if string or charset is null
	 */
	public RequestBuilder setBody(long jobId,int task,String string,Charset charset){
		buffer.clear();
		
		charsetName = charset.name(); // implicit null check
		
		buffer.putLong(jobId);
		buffer.putInt(task);
		bodyBuff = charset.encode(Objects.requireNonNull(string));
		
		return this;
	}


	/**
	 * Get the response of the request. This method is blocking.
	 * @return the response of the request. If the optional is empty, the request is too long
	 * @throws HttpException
	 * @throws IOException
	 * @throws ClosedChannelException if the channel is closed
	 * @throws IllegalStateException if the channel is not connected
	 */
	public Optional<HttpResponse> response() throws HttpException,IOException{
		if( sc.getRemoteAddress() == null){ 
			throw new IllegalStateException();
		}
		
		if(! sendRequest()){
			return Optional.empty();
		}
		return Optional.of(getResponse());
	}


	/**
	 * Send the request to the server.
	 * @throws IOException
	 * @return true if the request were sent or false if the request were bigger than 4096 bytes
	 */
	boolean sendRequest() throws IOException {
		buffer.flip();
		
		StringBuilder sb = new StringBuilder();
		sb.append(method)
		.append(" ")
		.append(ressource == null ? "/" : ressource)
		.append(" HTTP/1.1\r\n")
		.append("Host: ")
		.append(((InetSocketAddress)sc.getRemoteAddress()).getHostString())
		.append("\r\n");	// Building the request
		
		int contentLength = buffer.remaining();
		if(bodyBuff != null){
			contentLength += bodyBuff.remaining();
			headers.put("Content-Type", "application/json; charset="+charsetName);
			headers.put("Content-Length", Integer.toString(contentLength));
		}
		
		for(Entry <String,String> entry : headers.entrySet()){ 
			sb.append(entry.getKey());
			sb.append(": ");
			sb.append(entry.getValue());		//Writing each line of the header
			sb.append("\r\n");
		}
		sb.append("\r\n");
		
		
		ByteBuffer buff = charsetASCII.encode(sb.toString()); //Encoding the header to ascii
		int headerLength = buff.remaining();
		
		int total = headerLength + contentLength;
		
		if(total > REQUEST_SIZE){ //Check if the request is too long or not
			return false;
		}
		
		sc.write(buff);
		if(bodyBuff != null){
			sc.write(buffer);
			sc.write(bodyBuff);
		}
		
		send = true;
		return true;
	}


	/**
	 * Get the response of the request
	 * @return the response of the request send by the server
	 * @throws IOException
	 * @throws IllegalStateException if the request has not been send.
	 */
	HttpResponse getResponse() throws IOException {
		if(! send){ // check if the request has been send
			throw new IllegalStateException();
		}
		buffer.clear();
		HttpReader httpReader = new HttpReader(sc, buffer);
		HttpResponseHeader header = httpReader.readHeader();
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
				return null;
			}
			content = httpReader.readBytes(contentLength);
		}
		content.flip();
		return header.getCharset().decode(content).toString();

	}



}

