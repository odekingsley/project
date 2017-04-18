package fr.upem.jarset.server;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.Test;

import fr.upem.Util;
import fr.upem.http.HttpException;
import fr.upem.http.HttpReader;
import fr.upem.http.HttpResponseHeader;
import fr.upem.jarret.server.ResponseBuilder;
import fr.upem.jarret.server.ResponseBuilder.Code;

public class ResponseBuilderTest {

	@Test
	public void testResponseBuilder() {
		new ResponseBuilder(Code.OK);
	}

	@Test(expected=NullPointerException.class)
	public void testResponseBuilderNullCode() {
		new ResponseBuilder(null);
	}

	
	@Test
	public void testSetBody() throws HttpException, IOException {
		String body = "test";
		List<ByteBuffer> list = new ResponseBuilder(Code.OK).setBody(body, Util.getUtf8Charset()).get();
		int total = list.stream().mapToInt(ByteBuffer::remaining).sum();
		ByteBuffer buffer = list.stream().reduce(ByteBuffer.allocate(total), (a,b )-> a.put(b) );
		HttpReader reader = new HttpReader(null, buffer);
		HttpResponseHeader header = reader.readHeader();
		assertEquals(header.getCode(), Code.OK.getCode());
		assertEquals(Util.getUtf8Charset().name(), header.getCharset().name());
		assertEquals(body, header.getCharset().decode((ByteBuffer)reader.readBytes(header.getContentLength()).flip()).toString());
	}

	@Test(expected=NullPointerException.class)
	public void testSetBodyNullString() {
		new ResponseBuilder(Code.BadRequest).setBody(null, Util.getUtf8Charset());
	}
	
	@Test(expected=NullPointerException.class)
	public void testSetBodyNullCharset() {
		new ResponseBuilder(Code.BadRequest).setBody("test", null);
	}
	
	@Test
	public void testAddHeader() {
		new ResponseBuilder(Code.OK).addHeader("Content-Type", "json");
	}

	@Test
	public void testGet() throws IOException {
		
		
		List<ByteBuffer> list = new ResponseBuilder(Code.OK).get();
		int total = list.stream().mapToInt(ByteBuffer::remaining).sum();
		ByteBuffer buffer = list.stream().reduce(ByteBuffer.allocate(total), (a,b )-> a.put(b) );
		HttpReader reader = new HttpReader(null, buffer);
		HttpResponseHeader header = reader.readHeader();
		assertEquals(header.getCode(), Code.OK.getCode());
		
	}

}
