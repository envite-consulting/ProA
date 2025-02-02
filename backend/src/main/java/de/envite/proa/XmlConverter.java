package de.envite.proa;

import java.nio.charset.StandardCharsets;

public class XmlConverter {

    public static byte[] stringToBytes(String xml) {
        return xml.getBytes(StandardCharsets.UTF_8);
    }

    public static String bytesToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
