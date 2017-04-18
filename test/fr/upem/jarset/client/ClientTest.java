package fr.upem.jarset.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.upem.jarret.client.JarretClient;
import fr.upem.worker.Task;

public class ClientTest {

	private static final int port = 7778;
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
		Server server = Server.create(port);
		JarretClient client = JarretClient.create(new InetSocketAddress(port), "Ode");
		client.close();
		server.close();

	}

	@Test
	public void testRequestTask() throws IOException, InterruptedException, ExecutionException {
		Server server = Server.create(port);
		JarretClient client = JarretClient.create(new InetSocketAddress(port), "Ode");

		try{
			URL url = Paths.get("jarTest","WorkerTest.jar").toUri().toURL();
			Task task = new Task(1, 100, "1.0", url, "upem.jarret.worker.Normal");
			String json = "{\"JobId\":\""+task.getJobId()+"\",\"WorkerVersion\":\""+task.getVersion()+"\",\"WorkerURL\":\""+task.getWorkerUrl()+"\",\"WorkerClassName\":\""+task.getWorkerClassName()+"\",\"Task\":\""+task.getTask()+"\"}";
			Future<String> submit = executor.submit(() -> {
				try {
					return server.get(json);
				} catch (IOException e) {
					return "IO error";
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
	public void testManageTaskCumputation() throws IOException, InterruptedException, ExecutionException {
		Server server = Server.create(port);
		JarretClient client = JarretClient.create(new InetSocketAddress(port), "Ode");
		URL url = Paths.get("jarTest","WorkerTest.jar").toUri().toURL();
		String className = "upem.jarret.worker.ComputationError";
		Task task = new Task(1, 100, "1.0", url, className);
		try{
			String error = "Computation error";
			Future<String> future = executor.submit(() ->server.manageTask(error));
			Future<Boolean> future2 = executor.submit(() -> client.manageTask(task));
			String actual;
			try {
				actual = future.get(1,TimeUnit.SECONDS);
				assertEquals(null, actual);
				assertTrue(future2.get());
			} catch (TimeoutException e) {
				fail("time out exception");
			}
			

		}
		finally {	
			client.close();
			server.close();
		}
		
	}
	
	@Test
	public void testManageTaskInvalidJson() throws IOException, InterruptedException, ExecutionException {
		Server server = Server.create(port);
		JarretClient client = JarretClient.create(new InetSocketAddress(port), "Ode");
		URL url = Paths.get("jarTest","WorkerTest.jar").toUri().toURL();
		String className = "upem.jarret.worker.InvalidJson";
		Task task = new Task(1, 100, "1.0", url, className);
		try{
			String error = "Answer is not valid JSON";
			Future<String> future = executor.submit(() ->server.manageTask(error));
			Future<Boolean> future2 = executor.submit(() -> client.manageTask(task));
			String actual;
			try {
				actual = future.get(1,TimeUnit.SECONDS);
				assertEquals(null, actual);
				assertTrue(future2.get());
			} catch (TimeoutException e) {
				fail("time out exception");
			}
			

		}
		finally {	
			client.close();
			server.close();
		}
		
	}
	@Test
	public void testManageTaskNested() throws IOException, InterruptedException, ExecutionException {
		Server server = Server.create(port);
		JarretClient client = JarretClient.create(new InetSocketAddress(port), "Ode");
		URL url = Paths.get("jarTest","WorkerTest.jar").toUri().toURL();
		String className = "upem.jarret.worker.Nested";
		Task task = new Task(1, 100, "1.0", url, className);
		try{
			String error = "Answer is nested";
			Future<String> future = executor.submit(() ->server.manageTask(error));
			Future<Boolean> future2 = executor.submit(() -> client.manageTask(task));
			String actual;
			try {
				actual = future.get(1,TimeUnit.SECONDS);
				assertEquals(null, actual);
				assertTrue(future2.get());
			} catch (TimeoutException e) {
				fail("time out exception");
			}
			

		}
		finally {	
			client.close();
			server.close();
		}
		
	}
	@Test
	public void testManageTaskTooLong() throws IOException, InterruptedException, ExecutionException {
		Server server = Server.create(port);
		JarretClient client = JarretClient.create(new InetSocketAddress(port), "Ode");
		URL url = Paths.get("jarTest","WorkerTest.jar").toUri().toURL();
		String className = "upem.jarret.worker.TooLong";
		Task task = new Task(1, 100, "1.0", url, className);
		try{
			String error = "Too Long";
			Future<String> future = executor.submit(() ->server.manageTask(error));
			Future<Boolean> future2 = executor.submit(() -> client.manageTask(task));
			String actual;
			try {
				actual = future.get(5,TimeUnit.SECONDS);
				assertEquals(null, actual);
				assertTrue(future2.get());
			} catch (TimeoutException e) {
				fail("time out exception");
			}
			

		}
		finally {	
			client.close();
			server.close();
		}
		
	}



}
