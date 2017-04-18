package fr.upem.jarret.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import fr.upem.http.HttpException;
import fr.upem.http.HttpMethod;
import fr.upem.http.HttpResponse;
import fr.upem.http.RequestBuilder;
import fr.upem.jarret.JsonAnswer;
import fr.upem.jarret.JsonAnswerError;
import fr.upem.worker.Task;
import fr.upem.worker.WorkerManager;
import upem.jarret.worker.Worker;
public class JarretClient {
	
	private static final int BUFFER_SIZE = 4096;
	private final static Set<Class<?>> ALLOWED_JSON_TYPE ;
	
	static{
		ALLOWED_JSON_TYPE = new HashSet<>(Arrays.asList(String.class,Long.class,Integer.class,Double.class,Float.class,Boolean.class));
	}

	private final SocketChannel sc;
	private final String clientId;
	private final ByteBuffer buff;
	private final WorkerManager manager = new WorkerManager();
	private final ObjectMapper mapper = new ObjectMapper();
	private JarretClient(SocketChannel sc, String clientId, ByteBuffer buff) {
		this.sc = sc;
		this.clientId = clientId;
		this.buff = buff;	
	}

	public static JarretClient create(InetSocketAddress serverAddress, String clientId) throws IOException {
		SocketChannel sc = SocketChannel.open(serverAddress);
		return new JarretClient(sc, clientId, ByteBuffer.allocate(BUFFER_SIZE));
	}

	/**
	 * Asking a task from the server
	 * @return an Optional containing a task
	 * @throws HttpException 
	 * @throws IOException
	 */
	public Optional<Task> requestTask() throws HttpException, IOException {
		System.out.println("Demanding a task");
		HttpResponse response = new RequestBuilder(HttpMethod.GET, sc, buff)
				.setResource("Task")
				.response()
				.get();
		
		System.out.println("Response receive");
		String body = response.getBody().get();
		
		
		TypeFactory factory = TypeFactory.defaultInstance();
		MapType type = factory.constructMapType(HashMap.class, String.class, String.class);
		HashMap<String, String> map = mapper.readValue(body, type);
		String comeBack = map.get("ComeBackInSeconds");
		if(comeBack != null){
			try {
				System.out.println("have to wait :"+comeBack);
				Thread.sleep(Integer.parseInt(comeBack));
				return Optional.empty();
			} catch (NumberFormatException e) {
				System.err.println(comeBack+ " is not a valid Number");
				return Optional.empty();
			} catch (InterruptedException e) {
				System.err.println("Interrupted when waiting");
				return Optional.empty();
			}
		}
		try{
			return Optional.of(mapper.readValue(body, Task.class));
		}catch (JsonParseException|JsonMappingException e) {
			System.err.println("can not parse the body to a Task class : "+body);
			return Optional.empty();
		}

	}

	/**
	 * Try to manage a task
	 * @param task the task to manage
	 * @return true if the task has been managed successfully false otherwise
	 * @throws HttpException 
	 * @throws IOException
	 */
	public boolean manageTask(Task task) throws HttpException, IOException {
		System.out.println("Getting the worker");
		Optional<Worker> optional = manager.getOrCreate(task);
		if(! optional.isPresent()){
			return false;
		}
		System.out.println("Get the worker");
		Worker worker = optional.get();
		try {
			System.out.println("Invoking the method");
			String result = null;
			try{
				result = worker.compute(task.getTask());
			}catch (Exception e) {
				return error(task, "Computation error");
			}
			if(result == null){
				return error(task, "Computation error");
			}

			System.out.println("decoding compute result : "+result);
			Map<String, Object> answer = mapper.readValue(result, new TypeReference<Map<String, Object>>() {});
			boolean allMatch = answer.values().stream().allMatch(v -> ALLOWED_JSON_TYPE.contains(v.getClass()));
			if(! allMatch){
				return error(task, "Answer is nested");
			}
			JsonAnswer jsonAnswer = new JsonAnswer(task, clientId, answer);
			System.out.println("Encoding the answer");
			String body = mapper.writeValueAsString(jsonAnswer);
			System.out.println("sending the response");
			Optional<HttpResponse> responseOptional = new RequestBuilder(HttpMethod.POST, sc,buff)
					.setBody(task.getJobId(), (int)task.getTask(), body, Charset.forName("utf-8"))
					.setResource("Answer")
					.response();
			if(! responseOptional.isPresent()){
				return error(task, "Too Long");
			}
			return responseOptional.get().getHeader().getCode() == 200 ;

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		} catch (JsonParseException e) {
			return error(task, "Answer is not valid JSON");
		} catch (JsonMappingException e) {
			return error(task, "Answer is not valid JSON");
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean error(Task task,String error) throws HttpException, IOException {
		System.err.println(error);
		buff.clear();
		JsonAnswerError jsonAnswerError = new JsonAnswerError(task, clientId, error);
		try {
			String string = mapper.writeValueAsString(jsonAnswerError);
			return new RequestBuilder(HttpMethod.POST, sc,buff)
					.setResource("Answer")
					.setBody(task.getJobId(), (int) task.getTask(), string, Charset.forName("UTF-8"))
					.response().get().getHeader().getCode() == 200;
		} catch (JsonProcessingException e) {
			return false;
		}
	}

	public void close() throws IOException {
		sc.close();

	}

	public static void main(String[] args) throws IOException {
		if(args.length != 3){
			usage();
			return;
		}
		
		JarretClient client = create(new InetSocketAddress(args[1],Integer.parseInt(args[2])), args[0]);

		while(! Thread.interrupted()){
			try{
				Optional<Task> optional = Optional.empty();
				while(! optional.isPresent()){
					System.out.println("Ask a Task");
					optional = client.requestTask();
				}

				if( ! client.manageTask(optional.get())){
					System.err.println("An error has occured");
					return;
				}
			}catch (HttpException e) {
				System.err.println("An error has occured");
			}
			
		}
		
		System.out.println("End");
		client.close();


	}

	private static void usage() {
		System.err.println("Usage Client clientId host port");
		
	}
}
