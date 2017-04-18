package fr.upem;

import java.nio.charset.Charset;

public class Util {
	private static Charset ascii;
	private static Charset utf8;

	/**
	 * get an ascii Charset
	 * @return the charset
	 */
	public static Charset getAsciiCharset(){
		if(ascii == null){
			ascii = Charset.forName("ASCII");
		}
		return ascii;
	}
	/**
	 * Get an utf_8 charset
	 * @return the charset
	 */
	public static Charset getUtf8Charset(){
		if(utf8 == null){
			utf8 = Charset.forName("UTF-8");
		}
		return utf8;
	}

	
}
