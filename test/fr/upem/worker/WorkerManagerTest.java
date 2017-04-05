package fr.upem.worker;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.junit.Test;

import fr.upem.worker.WorkerManager.Worker;

public class WorkerManagerTest {

	@Test
	public void testWorkerManager(){
		new WorkerManager();
	}
	@Test
	public void testGetOrCreate() throws IOException {
		WorkerInfo workerInfo = new WorkerInfo("1.0", new URL("http://igm.univ-mlv.fr/~carayol/WorkerPrimeV1.jar"), "upem.workerprime.WorkerPrime");
		Optional<Worker> optional = new WorkerManager().getOrCreate(workerInfo);
		assertTrue(optional.isPresent());
	}
	
	@Test
	public void testGetOrCreateSame() throws IOException{
		WorkerInfo workerInfo = new WorkerInfo("1.0", new URL("http://igm.univ-mlv.fr/~carayol/WorkerPrimeV1.jar"), "upem.workerprime.WorkerPrime");
		WorkerManager manager = new WorkerManager();
		Optional<Worker> optional = manager.getOrCreate(workerInfo);
		assertEquals(optional.get(), manager.getOrCreate(workerInfo).get());
	}
	
	@Test(expected =NullPointerException.class)
	public void testGetOrCreateNull() throws IOException{
		new WorkerManager().getOrCreate(null);
	}
	
	@Test
	public void testGetOrCreateWrongClass() throws IOException{
		WorkerInfo workerInfo = new WorkerInfo("1.0", new URL("http://igm.univ-mlv.fr/~carayol/WorkerPrimeV1.jar"), "wrongClass");
		Optional<Worker> optional = new WorkerManager().getOrCreate(workerInfo);
		assertTrue(! optional.isPresent());
	}

}
