package fr.upem.jarset;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParseException;
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
import fr.upem.worker.WorkerManager.Worker;

public class Client {

	private final SocketChannel sc;
	private final String clientId;
	private final ByteBuffer buff;
	private final WorkerManager manager = new WorkerManager();

	private Client(SocketChannel sc, String clientId,ByteBuffer buff) {
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
		HttpResponse response = new RequestBuilder(HttpMethod.GET, sc, buff).setResource("Task").response();
		String body = response.getBody().get();
		System.out.println("Response receive");
		System.out.println(body);
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
				e.printStackTrace();
				return Optional.empty();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return Optional.empty();
			}
		}
		try{
			return Optional.of(mapper.readValue(body, Task.class));
		}catch (JsonParseException|JsonMappingException e) {
			e.printStackTrace();
			return Optional.empty();
		}

	}

	public boolean manageTask(Task task) throws IOException{
		System.out.println("Managing a task");
		System.out.println("Getting the worker");
		Optional<Worker> optional = manager.getOrCreate(task.getInfo());
		if(! optional.isPresent()){
			return false;
		}
		System.out.println("Get the worker");
		Worker worker = optional.get();
		try {
			Method method = worker.getWorkerClass().getMethod("compute",Integer.TYPE);
			System.out.println("Invoking the methode");
			String result = method.invoke(worker.getWorker(),(int)task.getTask()).toString();
			ObjectMapper mapper = new ObjectMapper();
			System.out.println("decoding the result :"+result);
			Map<String, Object> answer = mapper.readValue(result, new TypeReference<Map<String, Object>>() {});
			// TODO champs de type object
			JsonAnswer jsonAnswer = new JsonAnswer(task, clientId, answer);
			System.out.println("Encoding the answer");
			String body = mapper.writeValueAsString(jsonAnswer);
			System.out.println("sending the response");
			int code = new RequestBuilder(HttpMethod.POST, sc,buff)
					.setBody(task.getJobId(), (int)task.getTask(), body, Charset.forName("utf-8"))
					.setResource("Answer")
					.response().getHeader().getCode();
			System.out.println(code);
			return code == 200;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return false;
		} catch (InvocationTargetException e) {
			computationError(task);
			e.printStackTrace();
			return false ;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		} catch (JsonParseException e) {
			e.printStackTrace();
			return false;
		} catch (JsonMappingException e) {
			notValidJson();
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private void notValidJson() {
		// TODO Auto-generated method stub

	}

	private void computationError(Task task) {
		// TODO Auto-generated method stub

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
