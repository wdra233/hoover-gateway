package com.eric.projects.hoover.core.utils;

import java.nio.charset.Charset;

public class BodyConverter {

    public static String convertBodyToString(byte[] body) {
        if (body == null) return null;
        return new String(body, Charset.forName("UTF-8"));
    }

    public static byte[] convertStringToBody(String body) {
        if (body == null) return null;
        return body.getBytes(Charset.forName("UTF-8"));
    }

}
