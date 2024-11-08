package de.envite.proa.startup;

import de.envite.proa.entities.User;
import de.envite.proa.usecases.authentication.AuthenticationUsecase;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

import io.github.cdimascio.dotenv.Dotenv;

@Startup
@ApplicationScoped
public class UserInitializer {

	@Inject
	AuthenticationUsecase usecase;

	private final List<String> admins = List.of("philipp.hehnle@envite.de", "jonathan.wagner@envite.de",
			"roberto.cortini@envite.de", "fabian.naether@envite.de");

	@PostConstruct
	public void init() {
		Dotenv dotenv = Dotenv.load();

		admins.forEach(email -> {
			if (usecase.findByEmail(email) == null) {
				User user = new User();
				user.setEmail(email);

				String[] name = email.split("@")[0].split("\\.");
				String firstName = name[0];
				String lastName = name[1];
				user.setFirstName(firstName.substring(0, 1).toUpperCase() + firstName.substring(1));
				user.setLastName(lastName.substring(0, 1).toUpperCase() + lastName.substring(1));

				String password = dotenv.get(firstName.toUpperCase() + "_PW", null);
				if (password == null) {
					return;
				}
				user.setPassword(password);
				user.setRole("Admin");
				usecase.register(user);
			}
		});
	}
}
