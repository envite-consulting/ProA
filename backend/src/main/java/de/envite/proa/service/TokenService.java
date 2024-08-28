package de.envite.proa.service;

import de.envite.proa.entities.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class TokenService {

	public String generateToken(User user) {
		Set<String> groups = new HashSet<>();
		groups.add("User");

		return Jwt //
				.issuer("your-issuer") //
				.upn(user.getEmail()) //
				.groups(groups) //
				.claim("email", user.getEmail()) //
				.sign();
	}
}
