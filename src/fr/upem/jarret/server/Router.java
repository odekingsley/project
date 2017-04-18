package fr.upem.jarret.server;

import java.util.HashMap;
import java.util.Optional;

import fr.upem.http.HttpMethod;

public class Router {
	private static class RouteEntry{
		final HttpMethod method;
		final String ressource;
		
		public RouteEntry(HttpMethod method , String ressource) {
			this.method = method;
			this.ressource = ressource;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((method == null) ? 0 : method.hashCode());
			result = prime * result + ((ressource == null) ? 0 : ressource.hashCode());
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
			RouteEntry other = (RouteEntry) obj;
			if (method != other.method)
				return false;
			if (ressource == null) {
				if (other.ressource != null)
					return false;
			} else if (!ressource.equals(other.ressource))
				return false;
			return true;
		}
		
	}

	private final HashMap<RouteEntry,Route> routes = new HashMap<>();
	
	public final void register(String method,String ressource,Route route){
		HttpMethod httpMethod = HttpMethod.fromString(method);
		RouteEntry entry = new RouteEntry(httpMethod, ressource);
		
		routes.put(entry, route);
	}
	
	public final Optional<Route> get(String method,String ressource){
		HttpMethod httpMethod = HttpMethod.fromString(method);
		RouteEntry entry = new RouteEntry(httpMethod, ressource);
		
		return Optional.ofNullable(routes.get(entry));
	}
	
	
}
