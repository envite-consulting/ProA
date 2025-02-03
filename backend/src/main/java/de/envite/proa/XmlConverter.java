package de.envite.proa;

import java.nio.charset.StandardCharsets;

public class XmlConverter {

    public static byte[] stringToBytes(String xml) {
        if (xml == null) {
            return null;
        }
        return xml.getBytes(StandardCharsets.UTF_8);
    }

    public static String bytesToString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
