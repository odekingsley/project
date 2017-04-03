package fr.upem.http;

import static fr.upem.http.HttpException.ensure;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HttpRequestHeader extends HttpHeader {
	
	private static String LIST_SUPPORTED_METHOD[] = new String[]{"POST","GET"};
	public static final Set<String> SUPPORTED_METHODS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(LIST_SUPPORTED_METHOD)));

	private final String method;
	private final String ressource;
	
    private HttpRequestHeader(String response,String version,int code,Map<String, String> fields,String method,String ressource) throws HttpException {
    	super(response, version, code, fields);
		this.method = method;
		this.ressource = ressource;
    	
    }
    
    public static HttpRequestHeader create(String response, Map<String,String> fields) throws HttpException {
        String[] tokens = response.split(" ");
        // Treatment of the response line
        ensure(tokens.length >= 2, "Badly formed request:\n" + response);
        String method = tokens[0];
        String ressource = tokens[1];
        String version = tokens[2];
        ensure(SUPPORTED_METHODS.contains(method), "Unsupported method in response:\n" + method);
        ensure(HttpRequestHeader.SUPPORTED_VERSIONS.contains(version), "Unsupported version in response:\n" + response);
        
        int code = 0;
        try {
            code = Integer.valueOf(tokens[1]);
            ensure(code >= 100 && code < 600, "Invalid code in response:\n" + response);
        } catch (NumberFormatException e) {
            ensure(false, "Invalid response:\n" + response);
        }
        Map<String,String> fieldsCopied = new HashMap<>();
        for (String s : fields.keySet())
            fieldsCopied.put(s,fields.get(s).trim());
        return new HttpRequestHeader(response,version,code,fieldsCopied,method,ressource);
    }

	public String getRessource() {
		return ressource;
	}

	public String getMethod() {
		return method;
	}
}
