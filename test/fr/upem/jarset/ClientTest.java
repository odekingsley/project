package fr.upem.jarset;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.upem.worker.Task;
import fr.upem.worker.WorkerInfo;

public class ClientTest {

	ExecutorService executor;

	@Before
	public void init(){
		executor = Executors.newFixedThreadPool(10);
	}

	@After
	public void close(){
		executor.shutdown();
	}
	@Test
	public void testCreate() throws IOException {
		Server server = Server.create(7778);
		Client client = Client.create(new InetSocketAddress(7778), "Ode");
		client.close();
		server.close();

	}

	@Test
	public void testRequestTask() throws IOException, InterruptedException, ExecutionException {
		Server server = Server.create(7778);
		Client client = Client.create(new InetSocketAddress(7778), "Ode");

		try{
			WorkerInfo info = new WorkerInfo("1.0", new URL("http://igm.univ-mlv.fr/~carayol/WorkerPrimeV1.jar"), "upem.workerprime.WorkerPrime");
			Task task = new Task(23571113, info, 100);
			String json = "{\"JobId\":\""+task.getJobId()+"\",\"WorkerVersion\":\""+info.getVersion()+"\",\"WorkerURL\":\""+info.getUrl()+"\",\"WorkerClassName\":\""+info.getClassName()+"\",\"Task\":\""+task.getTask()+"\"}";
			Future<String> submit = executor.submit(() -> {
				try {
					return server.get(json);
				} catch (IOException e) {
					return null;
				}});
			Future<Optional<Task>> future = executor.submit(() -> client.requestTask());
			assertEquals(null, submit.get());
			Optional<Task> optional = future.get();
			assertTrue(optional.isPresent());
			assertEquals(task, optional.get());
			
			

		}
		finally {	
			client.close();
			server.close();
		}
	}

	@Test
	public void testManageTask() {
		fail("Not yet implemented");
	}



}
