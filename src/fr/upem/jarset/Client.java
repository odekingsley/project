package fr.upem.jarset;

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

import fr.upem.http.HttpMethod;
import fr.upem.http.HttpResponse;
import fr.upem.http.RequestBuilder;
import fr.upem.worker.Task;
import fr.upem.worker.WorkerManager;
import upem.jarret.worker.Worker;
public class Client {
	
	private final static Set<Class<?>> ALLOWED_JSON_TYPE ;
	
	static{
		ALLOWED_JSON_TYPE = new HashSet<>(Arrays.asList(String.class,Long.class,Integer.class,Double.class,Float.class,Boolean.class));
	}

	private final SocketChannel sc;
	private final String clientId;
	private final ByteBuffer buff;
	private final WorkerManager manager = new WorkerManager();

	private Client(SocketChannel sc, String clientId, ByteBuffer buff) {
		this.sc = sc;
		this.clientId = clientId;
		this.buff = buff;	
	}

	public static Client create(InetSocketAddress serverAddress, String clientId) throws IOException {
		SocketChannel sc = SocketChannel.open(serverAddress);
		return new Client(sc, clientId, ByteBuffer.allocate(1024));
	}

	public Optional<Task> requestTask() throws IOException{
		System.out.println("Demanding a task");
		HttpResponse response = new RequestBuilder(HttpMethod.GET, sc, buff).setResource("Task").response().get();
		String body = response.getBody().get();
		System.out.println("Response receive");
		ObjectMapper mapper = new ObjectMapper();
		TypeFactory factory = TypeFactory.defaultInstance();
		MapType type = factory.constructMapType(HashMap.class, String.class, String.class);
		HashMap<String, String> map = mapper.readValue(body, type);
		String comeBack = map.get("ComeBackInSeconds");
		if(comeBack != null){
			try {
				System.out.println("have to wait :"+comeBack);
				wait(Integer.parseInt(comeBack));
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

	public boolean manageTask(Task task) throws IOException{
		System.out.println("Getting the worker");
		Optional<Worker> optional = manager.getOrCreate(task);
		if(! optional.isPresent()){
			return false;
		}
		System.out.println("Get the worker");
		Worker worker = optional.get();
		try {
			System.out.println("Invoking the methode");
			String result = null;
			try{
				result = worker.compute(task.getTask());
			}catch (Exception e) {
				return error(task, "Computation error");
			}

			ObjectMapper mapper = new ObjectMapper();
			System.out.println("decoding compute result : "+result);
			Map<String, Object> answer = mapper.readValue(result, new TypeReference<Map<String, Object>>() {});
			boolean allMatch = answer.values().stream().allMatch(v -> ALLOWED_JSON_TYPE.contains(v.getClass()));
			if(! allMatch){
				return error(task, "Answer is nested");
			}
			Answer jsonAnswer = new JsonAnswer(task, clientId, answer);
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

	private boolean error(Task task,String error) throws IOException {
		System.err.println(error);
		buff.clear();
		JsonAnswerError jsonAnswerError = new JsonAnswerError(task, clientId, error);
		ObjectMapper mapper = new ObjectMapper();
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
		Client client = create(new InetSocketAddress("ns3001004.ip-5-196-73.eu",8080), "kking");
		Optional<Task> optional = Optional.empty();
		while(! optional.isPresent()){
			System.out.println("Ask a Task");
			optional = client.requestTask();
		}

		System.out.println(client.manageTask(optional.get()));

		client.close();


	}
}
