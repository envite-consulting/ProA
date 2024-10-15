package de.envite.proa.service;

import de.envite.proa.entities.Role;
import de.envite.proa.entities.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class TokenService {

	public String generateToken(User user, String role) {
        Set<String> groups = new HashSet<>();
		groups.add(role);

		return Jwt //
				.issuer("proa-issuer") //
				.upn(user.getEmail()) //
				.groups(groups) //
				.claim("userId", user.getId()) //
				.sign();
	}
}
