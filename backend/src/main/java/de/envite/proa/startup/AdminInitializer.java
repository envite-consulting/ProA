package de.envite.proa.startup;

import de.envite.proa.entities.authentication.User;
import de.envite.proa.usecases.authentication.AuthenticationUsecase;
import de.envite.proa.usecases.user.UserUsecase;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Startup
@ApplicationScoped
public class AdminInitializer {

	@Inject
	UserUsecase userUsecase;

	@Inject
	AuthenticationUsecase authenticationUsecase;

	@Inject
	@ConfigProperty(name = "admin.email")
	String adminEmail;

	@Inject
	@ConfigProperty(name = "admin.password")
	String adminPassword;

	@PostConstruct
	public void init() {
		if (userUsecase.findByEmail(adminEmail) != null) {
			return;
		}
		User user = new User();
		user.setEmail(adminEmail);
		user.setPassword(adminPassword);
		user.setRole("Admin");
		try {
			authenticationUsecase.register(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
