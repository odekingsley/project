package fr.upem.http;

import static fr.upem.http.HttpException.ensure;

import java.util.HashMap;
import java.util.Map;

public class HttpResponseHeader extends HttpHeader {

	
    private HttpResponseHeader(String response,String version,int code,Map<String, String> fields) throws HttpException {
    	super(response, version, code, fields);
    }
    
    public static HttpResponseHeader create(String response, Map<String,String> fields) throws HttpException {
        String[] tokens = response.split(" ");
        // Treatment of the response line
        ensure(tokens.length >= 2, "Badly formed response:\n" + response);
        String version = tokens[0];
        ensure(HttpResponseHeader.SUPPORTED_VERSIONS.contains(version), "Unsupported version in response:\n" + response);
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
        return new HttpResponseHeader(response,version,code,fieldsCopied);
    }
}
