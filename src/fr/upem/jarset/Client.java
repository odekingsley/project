package fr.upem.jarset;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
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
		// TODO Auto-generated method stub
		SocketChannel sc = SocketChannel.open(serverAddress);
		return new Client(sc, clientId, ByteBuffer.allocate(1024));
	}

	public Optional<Task> requestTask() throws IOException{
		HttpResponse response = new RequestBuilder(HttpMethod.GET, sc).setResource("Task").response();
		String body = response.getBody();

		ObjectMapper mapper = new ObjectMapper();
		TypeFactory factory = TypeFactory.defaultInstance();
		MapType type = factory.constructMapType(HashMap.class, String.class, String.class);
		HashMap<String, String> map = mapper.readValue(body, type);
		String comeBack = map.get("ComeBackInSeconds");
		if(comeBack != null){
			try {
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

	public void manageTask(Task task){
		Optional<Worker> optional = manager.getOrCreate(task.getInfo());
		if(! optional.isPresent()){
			return;
		}
		Worker worker = optional.get();
		try {
			
			Method method = worker.getWorkerClass().getMethod("compute", Integer.class);
			String result = method.invoke(worker.getWorker(), task.getTask()).toString();
			ObjectMapper mapper = new ObjectMapper();
			Answer answer = mapper.readValue(result, Answer.class);
			// TODO champs de type object
			JsonAnswer jsonAnswer = new JsonAnswer(task, clientId, answer);
			String body = mapper.writeValueAsString(jsonAnswer);
			new RequestBuilder(HttpMethod.POST, sc);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return;
		} catch (InvocationTargetException e) {
			computationError(task);
			e.printStackTrace();
			return;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return;
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			notValidJson();
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void notValidJson() {
		// TODO Auto-generated method stub
		
	}

	private void computationError(Task task) {
		// TODO Auto-generated method stub
		
	}
}
