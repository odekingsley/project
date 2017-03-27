package fr.upem.http;

import java.io.IOException;

public class HttpException extends IOException {

    private static final long serialVersionUID = -1810727803680020453L;

    public HttpException() {
        super();
    }

    public HttpException(String s) {
        super(s);
    }

    public static void ensure(boolean b, String string) throws HttpException {
        if (!b)
            throw new HttpException(string);

    }
}