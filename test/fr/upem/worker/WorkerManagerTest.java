package fr.upem.worker;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.Test;

import upem.jarret.worker.Worker;

public class WorkerManagerTest {

	@Test
	public void testWorkerManager(){
		new WorkerManager();
	}
	@Test
	public void testGetOrCreate() throws IOException {
		URL url = Paths.get("jarTest","WorkerTest.jar").toUri().toURL();
		//WorkerInfo workerInfo = new WorkerInfo("1.0", url, "upem.jarret.worker.Normal");
		Task task = new Task(1, 100, "1.0", url, "upem.jarret.worker.Normal");
		Optional<Worker> optional = new WorkerManager().getOrCreate(task);
		assertTrue(optional.isPresent());
	}
	
	@Test
	public void testGetOrCreateSame() throws IOException{
		URL url = Paths.get("jarTest","WorkerTest.jar").toUri().toURL();
		Task task = new Task(1, 100, "1.0", url, "upem.jarret.worker.Normal");
		WorkerManager manager = new WorkerManager();
		Optional<Worker> optional = manager.getOrCreate(task);
		assertTrue(optional.get() == manager.getOrCreate(task).get());
	}
	
	
	@Test
	public void testGetOrCreateNotSame() throws IOException{
		
		URL url = Paths.get("jarTest","WorkerTest.jar").toUri().toURL();
		Task task = new Task(1, 100, "1.0", url, "upem.jarret.worker.Normal");
		
		Task task2 = new Task(1, 120, "2.0", url, "upem.jarret.worker.Normal");
		WorkerManager manager = new WorkerManager();
		Optional<Worker> optional = manager.getOrCreate(task);
		assertFalse(optional.get() == manager.getOrCreate(task2).get());
	}
	
	@Test(expected =NullPointerException.class)
	public void testGetOrCreateNull() throws IOException{
		new WorkerManager().getOrCreate(null);
	}
	
	@Test
	public void testGetOrCreateWrongClass() throws IOException{
		URL url = Paths.get("jarTest","WorkerTest.jar").toUri().toURL();
		//WorkerInfo workerInfo = new WorkerInfo("1.0", url, "upem.jarret.worker.WrongClass");
		Task task = new Task(1, 100, "1.0", url, "upem.jarret.worker.WrongClass");

		Optional<Worker> optional = new WorkerManager().getOrCreate(task);
		assertTrue(! optional.isPresent());
	}

}
