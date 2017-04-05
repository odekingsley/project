package fr.upem.http;

import static fr.upem.http.HttpException.ensure;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * This class represent the header of an Http Request.
 * It is contains of the method, the resource, the version, and the fields of
 * the request.
 * @author ode
 *
 */
public class HttpRequestHeader extends HttpHeader {
	
	private static String LIST_SUPPORTED_METHOD[] = new String[]{"POST","GET"};
	public static final Set<String> SUPPORTED_METHODS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(LIST_SUPPORTED_METHOD)));

	private final String method;
	private final String ressource;
	private final String request;
	
    private HttpRequestHeader(String request,String version,Map<String, String> fields,String method,String ressource) throws HttpException {
    	super(version, fields);
		this.request = request;
		this.method = method;
		this.ressource = ressource;
    	
    }
    
    
    /**
     * Create an HttpRequestHeader.
     * @param request the first line of the request like : "GET Task HTTP/1.1"
     * @param fields the fields of the request.
     * @return the HttpRequestHeader
     * @throws HttpException if the request is badly formed or the method or the version is unsupported.
     */
    public static HttpRequestHeader create(String request, Map<String,String> fields) throws HttpException {
        String[] tokens = request.split(" ");
        ensure(tokens.length == 3, "Badly formed request:\n" + request);
        String method = tokens[0];
        String ressource = tokens[1];
        String version = tokens[2];
        ensure(SUPPORTED_METHODS.contains(method), "Unsupported method in request:\n" + method);
        ensure(HttpRequestHeader.SUPPORTED_VERSIONS.contains(version), "Unsupported version in request:\n" + request);
        
        Map<String,String> fieldsCopied = new HashMap<>();
        for (String s : fields.keySet())
            fieldsCopied.put(s,fields.get(s).trim());
        return new HttpRequestHeader(request,version,fieldsCopied,method,ressource);
    }

    /**
     * Get the resource of the request.
     * @return the resource of the request
     */
	public String getRessource() {
		return ressource;
	}

	/**
	 * Get the method of the request.
	 * @return the method of the request
	 */
	public String getMethod() {
		return method;
	}

	
	/**
	 * Get the first line of the request
	 * @return the first line of the request
	 */
	public String getRequest() {
		return request;
	}
}
