package de.envite.proa;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class XmlConverterTest {

	public static final String XML = "<xml></xml>";
	public static final byte[] XML_BYTES = XML.getBytes(StandardCharsets.UTF_8);

	@Test
	void testClassInitialization() {
		XmlConverter xmlConverter = new XmlConverter();
		assertNotNull(xmlConverter);
	}

	@Test
	void testStringToBytes() {
		assertArrayEquals(XML_BYTES, XmlConverter.stringToBytes(XML));
	}

	@Test
	void testStringToBytes_Null() {
		assertNull(XmlConverter.stringToBytes(null));
	}

	@Test
	void testBytesToString() {
		assertEquals(XML, XmlConverter.bytesToString(XML_BYTES));
	}

	@Test
	void testBytesToString_Null() {
		assertNull(XmlConverter.bytesToString(null));
	}
}
