package de.envite.proa.rest;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class RedirectionResourceTest {

	@InjectMocks
	private RedirectionResource redirectionResource;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	/*@Test
	public void testServeVueApp() {
		Response response = redirectionResource.serveVueApp();

		assertEquals(Response.Status.SEE_OTHER.getStatusCode(), response.getStatus());

		assertEquals("/", response.getHeaderString("Location"));
	}*/
}
