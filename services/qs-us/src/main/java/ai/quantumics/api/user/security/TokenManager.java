package ai.quantumics.api.user.security;

import ai.quantumics.api.user.model.QsUserV2;
import ai.quantumics.api.user.service.UserServiceV2;
import ai.quantumics.api.user.vo.RefreshTokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class TokenManager implements Serializable {

	@Value("${jwt.secret.key}")
	private String secret;

	@Value("${jwt.tokenExpirationMs}")
	private Long tokenExpiration;

	@Value("${jwt.refreshTokenExpirationMs}")
	private Long refreshTokenExpirationMs;

	@Autowired
	private UserServiceV2 userServiceV2;

	//retrieve username from jwt token
	public String getUserEmailFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	//retrieve expiration date from jwt token
	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

    //for retrieveing any information from token we will need the secret key
	private Claims getAllClaimsFromToken(String token) {
		System.out.println("Secret key : "+ secret);
		return Jwts.parser().setSigningKey(secret.getBytes(StandardCharsets.UTF_8)).parseClaimsJws(token).getBody();
	}

	//check if the token has expired
	public Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	//generate token for user
	public String generateToken(QsUserV2 user) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("userRole", user.getUserRole());
		claims.put("userId", user.getUserId());
		return getToken(claims, user.getUserEmail());
	}

	//while creating the token -
	//1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
	//2. Sign the JWT using the HS512 algorithm and secret key.
	//3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
	//   compaction of the JWT to a URL-safe string 
	private String getToken(Map<String, Object> claims, String subject) {
		System.out.println("Secret key:" + secret);
		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + tokenExpiration.longValue()))
				.signWith(SignatureAlgorithm.HS512, secret.getBytes(StandardCharsets.UTF_8)).compact();

	}

	public String generateRefreshToken(QsUserV2 user) {
		Map<String, Object> claims = new HashMap<>();
		return getRefreshToken(claims, user.getUserEmail());
	}

	private String getRefreshToken(Map<String, Object> claims, String subject) {
		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs.longValue()))
				.signWith(SignatureAlgorithm.HS512, secret.getBytes(StandardCharsets.UTF_8)).compact();
	}

	//validate token
	public Boolean validateToken(String token) throws SQLException {
		try {
			if (!isTokenExpired(token)) {
				final String userEmail = getUserEmailFromToken(token);
				if (StringUtils.isNoneEmpty(userEmail)) {
					Claims claims = getAllClaimsFromToken(token);
					QsUserV2 userDetails = userServiceV2.getUserByEmail(userEmail);
					if (userEmail.equals(userDetails.getUserEmail()) && !claims.isEmpty()
							&& claims.get("userId").equals(userDetails.getUserId())
							&& claims.get("userRole").equals(userDetails.getUserRole())) {
						return true;
					}
				}
			}
		} catch (JwtException e) {
			return false;
		}
		return false;
	}

	//Get new access token from refresh token
	public RefreshTokenResponse refreshAccessToken(String token) throws SQLException {
		RefreshTokenResponse refreshTokenResponse = new RefreshTokenResponse();
		refreshTokenResponse.setSuccess(false);
		try {
			if (!isTokenExpired(token)) {
				final String userEmail = getUserEmailFromToken(token);
				if (StringUtils.isNoneEmpty(userEmail) && !isTokenExpired(token)) {
					QsUserV2 userDetails = userServiceV2.getUserByEmail(userEmail);
					if (userEmail.equals(userDetails.getUserEmail())) {
						refreshTokenResponse.setToken(generateToken(userDetails));
						refreshTokenResponse.setRefreshToken(token);
						refreshTokenResponse.setSuccess(true);
						return refreshTokenResponse;
					}
				}
			}
		} catch (JwtException e) {
			return refreshTokenResponse;
		}
		return refreshTokenResponse;
	}

}
