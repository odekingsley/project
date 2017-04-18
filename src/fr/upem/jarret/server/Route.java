package fr.upem.jarret.server;

import java.nio.ByteBuffer;
import java.util.List;

import fr.upem.http.non.blocking.HttpRequest;

@FunctionalInterface
public interface Route {
	List<ByteBuffer> manage(HttpRequest request);
	
	static Route  getTask (){
		return r -> {
			//TODO send a task
			
			return null;
		};
		
	}
	
	static Route  postAnswer (){
		return r -> {
			//TODO post an answer
			
			return null;
		};
		
	}
}
