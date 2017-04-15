package fr.upem.worker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import upem.jarret.worker.Worker;


public class WorkerManager {

	private static class WorkerInfo{
		private long jobId;
		private String version;
		public WorkerInfo(long jobId, String version) {
			this.jobId = jobId;
			this.version = version;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (jobId ^ (jobId >>> 32));
			result = prime * result + ((version == null) ? 0 : version.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			WorkerInfo other = (WorkerInfo) obj;
			if (jobId != other.jobId)
				return false;
			if (version == null) {
				if (other.version != null)
					return false;
			} else if (!version.equals(other.version))
				return false;
			return true;
		}




	}


	private final HashMap<WorkerInfo, Worker> map = new HashMap<>();
	/**
	 * Get the worker corresponding to the {@link WorkerInfo} and create it if not exist.
	 * @return the corresponding {@link Worker}
	 * @throws IOException 
	 */
	public Optional<Worker> getOrCreate(Task task) throws IOException{
		WorkerInfo info = new WorkerInfo(task.getJobId(), task.getVersion());
		Worker worker = map.get(info);
		if(worker == null){
			String className = task.getWorkerClassName();	

			try {

				worker = WorkerFactory.getWorker(task.getWorkerUrl(), className);
				map.put(info, worker);
			} catch (ClassNotFoundException e) {
				System.err.println("the class "+className+" can not be found");
				return Optional.empty();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return Optional.empty();
			} catch (InstantiationException e) {
				System.err.println("Can not instantiate the class "+className);
				return Optional.empty();
			} catch (ClassCastException e) {
				System.err.println("Can not cast the class "+className+" to upem.jarret.worker.Worker");
				return Optional.empty();
			}






		}

		return Optional.of(worker);
	}
}

