package de.ruu.lib.keycloak.admin.validation;

import de.ruu.lib.keycloak.admin.validation.JwtTokenParser.TokenInfo;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link JwtTokenParser}.
 * 
 * @author r-uu
 * @since 2025-12-27
 */
class JwtTokenParserTest
{
	/**
	 * Sample JWT token for testing (manually created).
	 * Payload: {
	 *   "iss": "http://localhost:8080/realms/test-realm",
	 *   "sub": "user-123",
	 *   "aud": ["pragma-backend", "account"],
	 *   "exp": 1735680000,
	 *   "iat": 1735679700,
	 *   "preferred_username": "testuser",
	 *   "realm_access": {
	 *     "roles": ["task-read", "task-write"]
	 *   }
	 * }
	 */
	private static final String SAMPLE_TOKEN = createSampleToken();
	
	private static String createSampleToken()
	{
		// Header: {"alg":"RS256","typ":"JWT"}
		String header = base64UrlEncode("{\"alg\":\"RS256\",\"typ\":\"JWT\"}");
		
		// Payload with test data
		String payload = base64UrlEncode(
				"{" +
				"\"iss\":\"http://localhost:8080/realms/test-realm\"," +
				"\"sub\":\"user-123\"," +
				"\"aud\":[\"pragma-backend\",\"account\"]," +
				"\"exp\":1735680000," +
				"\"iat\":1735679700," +
				"\"preferred_username\":\"testuser\"," +
				"\"realm_access\":{\"roles\":[\"task-read\",\"task-write\"]}" +
				"}");
		
		// Signature (not validated in this simple parser)
		String signature = "fake-signature";
		
		return header + "." + payload + "." + signature;
	}
	
	private static String base64UrlEncode(String input)
	{
		return Base64.getUrlEncoder()
				.withoutPadding()
				.encodeToString(input.getBytes());
	}
	
	@Test
	void testParseToken_ValidToken()
	{
		TokenInfo tokenInfo = JwtTokenParser.parseToken(SAMPLE_TOKEN);
		
		assertThat(tokenInfo).isNotNull();
		assertThat(tokenInfo.issuer()).isEqualTo("http://localhost:8080/realms/test-realm");
		assertThat(tokenInfo.subject()).isEqualTo("user-123");
		assertThat(tokenInfo.preferredUsername()).isEqualTo("testuser");
		assertThat(tokenInfo.expirationTime()).isEqualTo(1735680000L);
		assertThat(tokenInfo.issuedAt()).isEqualTo(1735679700L);

		// Check audiences
		assertThat(tokenInfo.audiences()).contains("pragma-backend");
		assertThat(tokenInfo.audiences()).contains("account");
		assertThat(tokenInfo.audiences()).hasSize(2);

		// Check roles
		assertThat(tokenInfo.roles()).contains("task-read");
		assertThat(tokenInfo.roles()).contains("task-write");
		assertThat(tokenInfo.roles()).hasSize(2);
	}
	
	@Test
	void testParseToken_GetLifetime()
	{
		TokenInfo tokenInfo = JwtTokenParser.parseToken(SAMPLE_TOKEN);
		
		// Lifetime should be exp - iat = 1735680000 - 1735679700 = 300 seconds
		assertThat(tokenInfo.lifetimeSeconds()).isEqualTo(300L);
	}
	
	@Test
	void testParseToken_NullToken()
	{
		assertThatThrownBy(() -> JwtTokenParser.parseToken(null))
				.isInstanceOf(IllegalArgumentException.class);
	}
	
	@Test
	void testParseToken_BlankToken()
	{
		assertThatThrownBy(() -> JwtTokenParser.parseToken("   "))
				.isInstanceOf(IllegalArgumentException.class);
	}
	
	@Test
	void testParseToken_InvalidFormat_TwoParts()
	{
		assertThatThrownBy(() -> JwtTokenParser.parseToken("header.payload"))
				.isInstanceOf(IllegalArgumentException.class);
	}
	
	@Test
	void testParseToken_InvalidFormat_FourParts()
	{
		assertThatThrownBy(() -> JwtTokenParser.parseToken("header.payload.signature.extra"))
				.isInstanceOf(IllegalArgumentException.class);
	}
	
	@Test
	void testTokenInfo_IsExpired()
	{
		// Create a token that expired in the past
		String expiredPayload = base64UrlEncode(
				"{" +
				"\"iss\":\"http://localhost:8080/realms/test-realm\"," +
				"\"exp\":1000000000," +  // Year 2001 - definitely expired
				"\"iat\":999999700," +
				"\"realm_access\":{\"roles\":[]}" +
				"}");
		
		String expiredToken = "header." + expiredPayload + ".signature";
		TokenInfo tokenInfo = JwtTokenParser.parseToken(expiredToken);
		
		assertThat(tokenInfo.expired()).isTrue();
		assertThat(tokenInfo.remainingLifetimeSeconds()).isEqualTo(0L); // Should be 0, not negative
	}
	
	@Test
	void testTokenInfo_NotExpired()
	{
		// Create a token that expires in the future
		long futureExp = (System.currentTimeMillis() / 1000) + 3600; // +1 hour
		long futureIat = futureExp - 300; // 5 minutes ago
		
		String futurePayload = base64UrlEncode(
				"{" +
				"\"iss\":\"http://localhost:8080/realms/test-realm\"," +
				"\"exp\":" + futureExp + "," +
				"\"iat\":" + futureIat + "," +
				"\"realm_access\":{\"roles\":[]}" +
				"}");
		
		String futureToken = "header." + futurePayload + ".signature";
		TokenInfo tokenInfo = JwtTokenParser.parseToken(futureToken);
		
		assertThat(tokenInfo.expired()).isFalse();
		assertThat(tokenInfo.remainingLifetimeSeconds()).isGreaterThan(3500); // Should be close to 1 hour
		assertThat(tokenInfo.remainingLifetimeSeconds()).isLessThan(3700);
	}
	
	@Test
	void testParseToken_SingleAudience()
	{
		// Test with single audience string instead of array
		String singleAudPayload = base64UrlEncode(
				"{" +
				"\"iss\":\"http://localhost:8080/realms/test-realm\"," +
				"\"aud\":\"pragma-backend\"," +  // Single string, not array
				"\"exp\":1735680000," +
				"\"iat\":1735679700," +
				"\"realm_access\":{\"roles\":[]}" +
				"}");
		
		String token = "header." + singleAudPayload + ".signature";
		TokenInfo tokenInfo = JwtTokenParser.parseToken(token);
		
		assertThat(tokenInfo.audiences()).hasSize(1);
		assertThat(tokenInfo.audiences()).contains("pragma-backend");
	}
	
	@Test
	void testParseToken_NoRoles()
	{
		// Test token without realm_access claim
		String noRolesPayload = base64UrlEncode(
				"{" +
				"\"iss\":\"http://localhost:8080/realms/test-realm\"," +
				"\"sub\":\"user-123\"," +
				"\"exp\":1735680000," +
				"\"iat\":1735679700" +
				"}");
		
		String token = "header." + noRolesPayload + ".signature";
		TokenInfo tokenInfo = JwtTokenParser.parseToken(token);
		
		assertThat(tokenInfo.roles()).isNotNull();
		assertThat(tokenInfo.roles()).isEmpty();
	}
}
