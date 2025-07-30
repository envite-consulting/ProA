package de.envite.proa.rest;

import de.envite.proa.util.ResourceLoader;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class RedirectionResourceTest {

	@Mock
	private ResourceLoader resourceLoader;

	@InjectMocks
	private RedirectionResource redirectionResource;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testServeVueApp() {
		InputStream mockStream = new ByteArrayInputStream("<html>Test</html>".getBytes());
		when(resourceLoader.loadResource("META-INF/resources/index.html")).thenReturn(mockStream);

		Response response = redirectionResource.serveVueApp();

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

		verify(resourceLoader, times(1)).loadResource("META-INF/resources/index.html");
		verifyNoMoreInteractions(resourceLoader);
	}

	@Test
	public void testServeVueApp_NotFound() {
		when(resourceLoader.loadResource("META-INF/resources/index.html")).thenReturn(null);

		Response response = redirectionResource.serveVueApp();

		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

		verify(resourceLoader, times(1)).loadResource("META-INF/resources/index.html");
		verifyNoMoreInteractions(resourceLoader);
	}
}
