package fr.upem.worker;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import upem.jarret.worker.Worker;


public class WorkerFactory {

    /**
     *
     * @param url location of the jar
     * @param className name of the class implementing upem.jarret.worker.Worker
     * @return An instance of className from the jar located at URL
     * @throws java.net.MalformedURLException if the url is malformed
     * @throws java.lang.ClassNotFoundException if the class className was not found in the jar
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.InstantiationException if the creation of the instance failed
     * @throws java.lang.ClassCastException (unchecked) if the cast to upem.jarret.worker.Worker failed
     */
    public static Worker getWorker(URL url,String className) throws MalformedURLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
            URL urls[] = {url};
            URLClassLoader urlClassLoader = new URLClassLoader(urls,Thread.currentThread().getContextClassLoader());
            Class<?> theClass=Class.forName(className,true,urlClassLoader);
            theClass.newInstance();
            return (Worker) theClass.newInstance();

    }
}