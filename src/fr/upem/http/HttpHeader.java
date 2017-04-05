package fr.upem.http;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class HttpHeader {

	/**
	 * Supported versions of the HTTP Protocol
	 */
	private static final String[] LIST_SUPPORTED_VERSIONS = new String[]{"HTTP/1.0", "HTTP/1.1", "HTTP/1.2"};
	public static final Set<String> SUPPORTED_VERSIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(LIST_SUPPORTED_VERSIONS)));
	protected final String version;
	protected final Map<String, String> fields;

	HttpHeader(String version,Map<String, String> fields) {
        this.version = version;
        this.fields = Collections.unmodifiableMap(fields);
	}

	

	public String getVersion() {
	    return version;
	}

	

	public Map<String, String> getFields() {
	    return fields;
	}

	/**
	 * @return the value of the Content-Length field in the header
	 *         -1 if the field does not exists
	 * @throws HTTPError when the value of Content-Length is not a number
	 */
	public int getContentLength() throws HttpException {
	    String s = fields.get("Content-Length");
	    if (s == null) return -1;
	    else {
	        try {
	            return Integer.valueOf(s.trim());
	        } catch (NumberFormatException e) {
	            throw new HttpException("Invalid Content-Length field value :\n" + s);
	        }
	    }
	}

	/**
	 * @return the Content-Type
	 *         null if there is no Content-Type field
	 */
	public String getContentType() {
	    String s = fields.get("Content-Type");
	    if (s != null) {
	        return s.split(";")[0].trim();
	    } else
	        return null;
	}

	/**
	 * @return the charset corresponding to the Content-Type field
	 *         null if charset is unknown or unavailable on the JVM
	 */
	public Charset getCharset() {
	    Charset cs = null;
	    String s = fields.get("Content-Type");
	    if (s == null) return cs;
	    for (String t : s.split(";")) {
	        if (t.contains("charset=")) {
	            try {
	                cs= Charset.forName(t.split("=")[1].trim());
	            } catch (Exception e) {
	               // If the Charset is unknown or unavailable we turn null
	            }
	            return cs;
	        }
	    }
	    return cs;
	}

	/**
	 * @return true if the header correspond to a chunked response
	 */
	public boolean isChunkedTransfer() {
	    return fields.containsKey("Transfer-Encoding") && fields.get("Transfer-Encoding").trim().equals("chunked");
	}

	

}