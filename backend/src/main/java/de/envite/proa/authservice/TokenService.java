package de.envite.proa.authservice;

import de.envite.proa.entities.authentication.User;
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
