package fr.upem.worker;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Optional;


public class WorkerManager {
	
	public static class Worker{
		private final Class<?> workerClass;
		private final Object worker;

		public Worker(Class<?> workerClass, Object worker) {
			this.workerClass = workerClass;
			this.worker = worker;
		}

		public Object getWorker() {
			return worker;
		}

		public Class<?> getWorkerClass() {
			return workerClass;
		}
	}
	
	
	private final HashMap<WorkerInfo, Worker> map = new HashMap<>();
	/**
	 * Get the worker corresponding to the {@link WorkerInfo} and create it if not exist.
	 * @return the corresponding {@link Worker}
	 * @throws IOException 
	 */
	public Optional<Worker> getOrCreate(WorkerInfo info) throws IOException{
		Worker worker = map.get(info);
		if(worker == null){
			
			URL[] classLoaderUrls = new URL[]{info.getUrl()};
			try (URLClassLoader classLoader = new URLClassLoader(classLoaderUrls)){
				Class<?> workerClass = classLoader.loadClass(info.getClassName());
				Constructor<?> constructor = workerClass.getConstructor();
				Object object = constructor.newInstance();
				worker = new Worker(workerClass, object);
				map.put(info, worker);
				
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
				return Optional.empty();
			}
			
			
			
		}
		
		return Optional.of(worker);
	}
}

