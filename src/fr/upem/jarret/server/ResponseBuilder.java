package fr.upem.jarret.server;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Map.Entry;

import fr.upem.Util;
import fr.upem.http.RequestBuilder;

public class ResponseBuilder {
	
	public enum Code{
		OK(200,"OK"),BadRequest(400,"Bad Request");
		
		private final int code;
		private final String libelle;

		private Code(int code,String libelle) {
			this.code = code;
			this.libelle = libelle;
		}

		public int getCode() {
			return code;
		}

		public String getLibelle() {
			return libelle;
		}
	}

	private final Code code;
	private ByteBuffer bodyBuffer;
	private final HashMap<String, String> map = new HashMap<>();
	
	/**
	 * Construct a new ResponseBuilder.
	 * @param code The code of the responseBuilder
	 * @throws NullPointerException if the code is null
	 */
	public ResponseBuilder(Code code) {
		this.code = Objects.requireNonNull(code);
	}
	
	/**
	 * Set the body of the Response
	 * @param body the body 
	 * @param charset the charset of the body
	 * @throws NullPointerException if the body or the charset is null
	 * @return
	 */
	public ResponseBuilder setBody(String body, Charset charset){

		bodyBuffer = charset.encode(body);
		int contentLength = bodyBuffer.remaining();
		map.put("Content-Type", "application/json; charset="+charset.name());
		map.put("Content-Length", Integer.toString(contentLength));
		return this;
	}
	/**
	 * Add a header to the request. 
	 * @param header the property
	 * @param value the value
	 * @return the {@link RequestBuilder}
	 * @throws NullPointerException if header or value is null
	 */
	public ResponseBuilder addHeader(String header,String value){
		map.put(header,Objects.requireNonNull(value));
		return this;
	}
	
	public List<ByteBuffer> get(){
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/1.1 ")
		.append(code.code)
		.append(" ")
		.append(code.libelle)
		.append("\r\n");
		
		for(Entry <String,String> entry : map.entrySet()){ 
			sb.append(entry.getKey());
			sb.append(": ");
			sb.append(entry.getValue());
			sb.append("\r\n");
		}
		sb.append("\r\n");
		ArrayList<ByteBuffer> list = new ArrayList<>(); 
		

		list.add(Util.getAsciiCharset().encode(sb.toString()));
		
		if(bodyBuffer != null){
			list.add(bodyBuffer);
		}
		
		return list;
	}
	
}
