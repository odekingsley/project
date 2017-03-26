package fr.upem.worker;

import java.net.URLClassLoader;


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
	
	private final URLClassLoader classLoader = new URLClassLoader(null);
	
	/**
	 * Get the worker corresponding to the {@link WorkerInfo} and create it if not exist.
	 * @return the corresponding {@link Worker}
	 */
	public Worker getOrCreate(WorkerInfo info){
		
		return null;
	}
}
