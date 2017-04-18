package fr.upem.http.non.blocking;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import fr.upem.http.HttpException;
import fr.upem.http.HttpRequestHeader;

public class HttpReaderNonBlocking {
	private static enum State{
		Default,ReadR
	}
	private final Charset ASCII_CHARSET = Charset.forName("ASCII");
	private ByteBuffer buff;
	private State state = State.Default;
	private StringBuilder sb = new StringBuilder();
	private final List<String> lines = new ArrayList<>();
	private HttpRequestHeader header;
	private ByteBuffer bodyBuffer;

	public HttpReaderNonBlocking(ByteBuffer buff) {
		this.buff = buff;
	}

	public Optional<String> readCRLF(){
		buff.flip();
		while(buff.hasRemaining()){
			switch(state){
			case Default:
				if(buff.get() == '\r'){
					state = State.ReadR;
					break;
				}
				state = State.Default;
				break;

			case ReadR:
				if(buff.get() == '\n'){
					state = State.Default;
					int newLimit = buff.position();
					int limit = buff.limit();
					buff.position(0);
					buff.limit(newLimit);
					sb.append(ASCII_CHARSET.decode(buff));
					buff.limit(limit);
					buff.compact();
					sb.delete(sb.length() -2, sb.length());

					String line = sb.toString();
					sb = new StringBuilder();
					return Optional.of(line);
				}
				state = State.Default;
				break;
			default:
				throw new IllegalStateException();
			}
		}
		buff.flip();
		sb.append(ASCII_CHARSET.decode(buff));
		buff.clear();
		return Optional.empty();
	}
	
	public Optional<ByteBuffer> readByte(int size){
		if(bodyBuffer == null){
			bodyBuffer = ByteBuffer.allocate(size);
		}
		if(! bodyBuffer.hasRemaining()){
			return Optional.of(bodyBuffer);
		}
		
		buff.flip();
        int oldLimit = buff.limit();
        
        if(buff.remaining() > size) {
                buff.limit(size);
        }
        
        bodyBuffer.put(buff);
        buff.limit(oldLimit);
		buff.compact();
		if(bodyBuffer.hasRemaining()){
			return Optional.empty();
		}
		
		return Optional.of(bodyBuffer);
	}


	public Optional<HttpRequestHeader> readHeader() throws HttpException{
		if(header != null){
			return Optional.of(header);
		}
		Optional<String> optional = readCRLF();
		while(optional.isPresent()){
			System.out.println(optional.get() + " ok");
			if(optional.get().isEmpty()){
				createHeader();
				return Optional.of(header); 
			}
			lines.add(optional.get());
			optional = readCRLF();
		}

		return Optional.empty();

	}
	public Optional<HttpRequest>readRequest() throws HttpException{
		readHeader();
		HttpRequestHeader h = header;
		if(h == null){
			return Optional.empty();
		}
		int contentLength = h.getContentLength();
		if(contentLength == -1){
			header = null;
			return Optional.of(new HttpRequest(null, h));
		}
		Optional<ByteBuffer> readByte = readByte(contentLength);
		if(! readByte.isPresent()){
			return Optional.empty();
		}
		header = null;
		bodyBuffer = null;
		lines.clear();
		return Optional.of(new HttpRequest(readByte.get(), h));
	}

	private void createHeader() throws HttpException {
		String remove = lines.remove(0);
		HashMap<String, String> map = new HashMap<>();
		for(String line : lines){
			String[] token = line.split(":");
			if(token.length != 2){
				throw new HttpException();
			}
			map.merge(token[0], token[1], (a,b) -> a+";"+b);

		}

		header = HttpRequestHeader.create(remove,map);
	}

}
