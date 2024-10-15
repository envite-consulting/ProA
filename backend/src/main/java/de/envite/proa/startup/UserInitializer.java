package de.envite.proa.startup;

import de.envite.proa.entities.User;
import de.envite.proa.usecases.authentication.AuthenticationUsecase;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@Startup
@ApplicationScoped
public class UserInitializer {

    @Inject
    AuthenticationUsecase usecase;

    @PostConstruct
    public void init() {
        if (usecase.findByEmail("philipp.hehnle@envite.de") == null) {
            User philipp = new User();
            philipp.setEmail("philipp.hehnle@envite.de");
            philipp.setFirstName("Philipp");
            philipp.setLastName("Hehnle");
            philipp.setPassword("philipp_password");
            philipp.setRole("Admin");
            usecase.register(philipp);
        }
        if (usecase.findByEmail("jonathan.wagner@envite.de") == null) {
            User jonathan = new User();
            jonathan.setEmail("jonathan.wagner@envite.de");
            jonathan.setFirstName("Jonathan");
            jonathan.setLastName("Wagner");
            jonathan.setPassword("jonathan_password");
            jonathan.setRole("Admin");
            usecase.register(jonathan);
        }
    }
}
