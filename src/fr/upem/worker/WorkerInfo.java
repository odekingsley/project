package fr.upem.worker;

import java.net.URL;
import java.util.Objects;

public class WorkerInfo {
	private final String version;
	private final URL url;
	private final String className;

	public WorkerInfo(String version, URL url, String className) {
		this.version = Objects.requireNonNull(version);
		this.url = Objects.requireNonNull(url);
		this.className = Objects.requireNonNull(className);
	}

	public String getVersion() {
		return version;
	}

	public URL getUrl() {
		return url;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + className.hashCode();
		result = prime * result + url.hashCode();
		result = prime * result + version.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (!(obj instanceof WorkerInfo))
			return false;
		
		WorkerInfo other = (WorkerInfo) obj;
		return other.className.equals(className) && other.url.equals(url) && other.version.equals(version);
	}

	@Override
	public String toString() {
		return "WorkerInfo [version=" + version + ", url=" + url + ", className=" + className + "]";
	}
	
	
}
