package fr.upem.http.non.blocking;

import java.nio.ByteBuffer;

import fr.upem.http.HttpRequestHeader;

public class HttpRequest {

	private final ByteBuffer body;
	private final HttpRequestHeader header;

	public HttpRequest(ByteBuffer body, HttpRequestHeader header) {
		this.body = body;
		this.header = header;
	}

	public ByteBuffer getBody() {
		return body;
	}

	public HttpRequestHeader getHeader() {
		return header;
	}
}
