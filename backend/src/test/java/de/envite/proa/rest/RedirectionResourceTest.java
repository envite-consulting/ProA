package de.envite.proa.rest;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RedirectionResourceTest {

    @InjectMocks
    private RedirectionResource redirectionResource;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRedirectToLandingPage() {
        Response response = redirectionResource.redirectToLandingPage();

        assertEquals(Response.Status.SEE_OTHER.getStatusCode(), response.getStatus());

        assertEquals("/", response.getHeaderString("Location"));
    }
}
