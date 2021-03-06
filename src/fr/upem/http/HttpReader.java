package fr.upem.http;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class HttpReader {
	
	private static enum State{
		Default,ReadR,EndLine
	}
	
	private final Charset ASCII_CHARSET = Charset.forName("ASCII");
	private final SocketChannel sc;
	private final ByteBuffer buff;

	private State state = State.Default;

	public HttpReader(SocketChannel sc, ByteBuffer buff) {
		this.sc = sc;
		this.buff = buff;
	}

	
	
	private static void read(SocketChannel sc,ByteBuffer buffer) throws IOException{

		if(sc.read(buffer) == -1 ){
			throw new HttpException();
		}

	}

	/**
	 * @return The ASCII string terminated by CRLF
	 * <p>
	 * The method assume that buff is in write mode and leave it in write-mode
	 * The method never reads from the socket as long as the buffer is not empty
	 * @throws IOException HTTPException if the connection is closed before a line could be read
	 */
	public String readLineCRLF() throws IOException {
		StringBuilder stringBuilder = new StringBuilder();

		buff.flip();

		while(state != State.EndLine){

			if(! buff.hasRemaining()){
				buff.flip();
				stringBuilder.append(ASCII_CHARSET.decode(buff));
				buff.clear();
				read(sc, buff);
				buff.flip();
			}

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
					state = State.EndLine;
					int newLimit = buff.position();
					int limit = buff.limit();
					buff.position(0);
					buff.limit(newLimit);
					stringBuilder.append(ASCII_CHARSET.decode(buff));
					buff.limit(limit);
					buff.compact();
					break;
				}
				state = State.Default;
				break;
			default:
				throw new IllegalStateException();
			}
		}

		state = State.Default;
		stringBuilder.delete(stringBuilder.length() -2, stringBuilder.length());
		return stringBuilder.toString();
	}

	/**
	 * @return The HTTPHeader object corresponding to the header read
	 * @throws IOException HTTPException if the connection is closed before a header could be read
	 *                     if the header is ill-formed
	 */
	public HttpResponseHeader readHeader() throws IOException {
		String response = readLineCRLF();
		if(response.isEmpty()){
			throw new HttpException();
		}
		HashMap<String,String> map = new HashMap<>();
		String line = readLineCRLF();
		while(! line.isEmpty()){
			String[] strings = line.split(":");
			map.merge(strings[0], strings[1], (x , y) -> x + ";" + y);
			line = readLineCRLF();
		}
		
		return HttpResponseHeader.create(response, map);
	}
	
	
	public HttpRequestHeader readRequestHeader() throws IOException{
		String request = readLineCRLF();
		if(request.isEmpty()){
			throw new HttpException();
		}
		HashMap<String,String> map = new HashMap<>();
		String line = readLineCRLF();
		while(! line.isEmpty()){
			String[] strings = line.split(":");
			map.merge(strings[0], strings[1], (x , y) -> x + ";" + y);
			line = readLineCRLF();
		}
		return HttpRequestHeader.create(request, map);
	}
	

	/**
	 * @param size
	 * @return a ByteBuffer in write-mode containing size bytes read on the socket
	 * @throws IOException HTTPException is the connection is closed before all bytes could be read
	 */
	public ByteBuffer readBytes(int size) throws IOException {
        ByteBuffer buffResult = ByteBuffer.allocate(size);
        
        buff.flip();
        int oldLimit = buff.limit();
        
        if(buff.remaining() > size) {
                buff.limit(size);
        }
        
        buffResult.put(buff);
        while(buffResult.hasRemaining()) {
                if(sc.read(buffResult) == -1) {
                        throw new IOException();
                }
        }
        
        buff.limit(oldLimit);
        
        buff.compact();
        return buffResult;
}

	/**
	 * @return a ByteBuffer in write-mode containing a content read in chunks mode
	 * @throws IOException HTTPException if the connection is closed before the end of the chunks
	 *                     if chunks are ill-formed
	 */

	public ByteBuffer readChunks() throws IOException {
		List<ByteBuffer> buffers = new ArrayList<>();
		int nbByte = Integer.parseInt(this.readLineCRLF(),16);
		while(nbByte != 0){
			ByteBuffer readBytes = readBytes(nbByte + 2);
			readBytes.flip();
			readBytes.limit(readBytes.limit()-2);
			buffers.add(readBytes);
			nbByte = Integer.parseInt(readLineCRLF(),16);
		}
		int size = buffers.stream().mapToInt(bb -> bb.remaining()).sum();
		ByteBuffer result = ByteBuffer.allocate(size);
		for(ByteBuffer bb : buffers){
			result.put(bb);
		}
		return result;
	}
}