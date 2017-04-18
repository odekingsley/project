package fr.upem.http;

public enum HttpMethod {
	POST,GET;
	
	public static HttpMethod fromString(String string){
		
		if(string.equals("POST")){
			return POST;
		}
		if(string.equals("GET")){
			return GET;
		}
		throw new IllegalArgumentException("Unsuported method " + string);
	}
}
