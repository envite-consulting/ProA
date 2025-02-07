package de.envite.proa.authservice;

import de.envite.proa.entities.authentication.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TokenServiceTest {

	private static final Long USER_ID = 1L;
	private static final String EMAIL = "test@test.de";
	private static final String ROLE = "Admin";
	private static final String ISSUER = "proa-issuer";
	private static final String PUBLIC_KEY_PATH = "src/main/resources/publicKey.test.pem";

	private static User user;

	@InjectMocks
	private TokenService tokenService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		user = new User();
		user.setId(USER_ID);
		user.setEmail(EMAIL);
	}

	@Test
	void testGenerateToken() throws Exception {
		String token = tokenService.generateToken(user, ROLE);

		PublicKey publicKey = loadPublicKey(PUBLIC_KEY_PATH);
		Claims claims = Jwts
				.parser()
				.verifyWith(publicKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();

		assertNotNull(token, "Generated token should not be null.");
		assertEquals(EMAIL, claims.get("upn"), "UPN should be the user's email.");
		assertEquals(ISSUER, claims.getIssuer(), "Token should include the correct issuer.");
		assertEquals(USER_ID.toString(), claims.get("userId").toString(), "Token should include the user's ID.");
		assertEquals(ROLE, ((ArrayList<?>) claims.get("groups")).getFirst(),
				"Token should include the role in groups.");
	}

	public static PublicKey loadPublicKey(String filePath) throws Exception {
		String pem = new String(Files.readAllBytes(Paths.get(filePath)))
				.replace("-----BEGIN PUBLIC KEY-----", "")
				.replace("-----END PUBLIC KEY-----", "")
				.replaceAll("\\s", "");

		byte[] decodedKey = Base64.getDecoder().decode(pem);

		X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePublic(spec);
	}
}
