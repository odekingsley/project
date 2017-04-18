package upem.jarret.worker;

public interface Worker {

    // Return a JSON String with the result of computing task number taskNumber
    public String compute(int taskNumber);

    // Return the jobId of the job treated by this worker
    public long getJobId();

    // Return the total number of tasks for this job
    public int getNumberOfTasks();

    // Return the version number for this worker
    public String getVersion();

    // Return a simple a description of the job
    public String getJobDescription();
}