package de.ruu.lib.keycloak.admin.validation;

import de.ruu.lib.keycloak.admin.validation.KeycloakConfigValidator.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link KeycloakConfigValidator}.
 * 
 * @author r-uu
 * @since 2025-12-27
 */
class KeycloakConfigValidatorTest
{
	@Test
	void testValidateRoles_AllPresent()
	{
		Set<String> tokenRoles = Set.of("task-read", "task-write", "task-delete");
		Set<String> requiredRoles = Set.of("task-read", "task-write");
		
		RoleValidationResult result = KeycloakConfigValidator.validateRoles(tokenRoles, requiredRoles);
		
		assertThat(result.valid()).isTrue();
		assertThat(result.missingRoles()).isEmpty();
		assertThat(result.extraRoles()).hasSize(1);
		assertThat(result.extraRoles()).contains("task-delete");
	}
	
	@Test
	void testValidateRoles_MissingRoles()
	{
		Set<String> tokenRoles = Set.of("task-read");
		Set<String> requiredRoles = Set.of("task-read", "task-write", "task-delete");
		
		RoleValidationResult result = KeycloakConfigValidator.validateRoles(tokenRoles, requiredRoles);
		
		assertThat(result.valid()).isFalse();
		assertThat(result.missingRoles()).hasSize(2);
		assertThat(result.missingRoles()).contains("task-write");
		assertThat(result.missingRoles()).contains("task-delete");
		assertThat(result.extraRoles()).isEmpty();

		assertThat(result.recommendations()).isNotEmpty();
		assertThat(result.summary()).contains("Missing");
	}
	
	@Test
	void testValidateAudience_Valid()
	{
		Set<String> tokenAudiences = Set.of("pragma-backend", "account");
		String expectedAudience = "pragma-backend";
		
		AudienceValidationResult result = KeycloakConfigValidator
				.validateAudience(tokenAudiences, expectedAudience);
		
		assertThat(result.valid()).isTrue();
		assertThat(result.expectedAudience()).isEqualTo("pragma-backend");
	}
	
	@Test
	void testValidateAudience_Missing()
	{
		Set<String> tokenAudiences = Set.of("account");
		String expectedAudience = "pragma-backend";
		
		AudienceValidationResult result = KeycloakConfigValidator
				.validateAudience(tokenAudiences, expectedAudience);
		
		assertThat(result.valid()).isFalse();
		assertThat(result.recommendations()).isNotEmpty();
		assertThat(result.summary()).contains("does not contain");
	}
	
	@Test
	void testValidateRoleNamingConsistency_Consistent()
	{
		Set<String> roles = Set.of("task-read", "task-write", "taskgroup-read", "taskgroup-write");
		
		NamingConsistencyResult result = KeycloakConfigValidator
				.validateRoleNamingConsistency(roles);
		
		assertThat(result.consistent()).isTrue();
		assertThat(result.suspiciousRoles()).isEmpty();
		assertThat(result.suggestions()).isEmpty();
	}
	
	@Test
	void testValidateRoleNamingConsistency_Inconsistent()
	{
		// Use roles where "taskgroup-read" conflicts with "task-group-read"
		Set<String> roles = Set.of("task-read", "task-write", "taskgroup-read", "task-group-read");
		
		NamingConsistencyResult result = KeycloakConfigValidator
				.validateRoleNamingConsistency(roles);
		
		assertThat(result.consistent()).isFalse();
		assertThat(result.suspiciousRoles()).isNotEmpty();

		// Both "taskgroup-read" and "task-group-read" should be flagged as conflicting
		assertThat(result.recommendations()).isNotEmpty();
	}

	@Test
	void testValidateTokenLifetime_Optimal()
	{
		int accessTokenLifetime = 300;  // 5 minutes
		int refreshTokenLifetime = 1800; // 30 minutes
		
		TokenLifetimeValidationResult result = KeycloakConfigValidator
				.validateTokenLifetime(accessTokenLifetime, refreshTokenLifetime);
		
		assertThat(result.warnings()).isEmpty();
		assertThat(result.accessTokenLifetimeSeconds()).isEqualTo(300);
		assertThat(result.refreshTokenLifetimeSeconds()).isEqualTo(1800);
	}
	
	@Test
	void testValidateTokenLifetime_TooShort()
	{
		int accessTokenLifetime = 30;  // 30 seconds - too short
		int refreshTokenLifetime = 60;  // 1 minute - too short
		
		TokenLifetimeValidationResult result = KeycloakConfigValidator
				.validateTokenLifetime(accessTokenLifetime, refreshTokenLifetime);
		
		assertThat(result.warnings()).isNotEmpty();
		assertThat(result.summary()).contains("warning");

		// Should have warnings about short lifetimes
		assertThat(result.warnings().stream()
				.anyMatch(w -> w.contains("very short"))).isTrue();
	}
	
	@Test
	void testValidateTokenLifetime_RefreshShorterThanAccess()
	{
		int accessTokenLifetime = 600;  // 10 minutes
		int refreshTokenLifetime = 300; // 5 minutes - shorter than access!
		
		TokenLifetimeValidationResult result = KeycloakConfigValidator
				.validateTokenLifetime(accessTokenLifetime, refreshTokenLifetime);
		
		assertThat(result.warnings()).isNotEmpty();
		assertThat(result.warnings().stream()
				.anyMatch(w -> w.contains("shorter than access token"))).isTrue();
	}
	
	@Test
	void testValidationReport_FullyValid()
	{
		RoleValidationResult roleValidation = KeycloakConfigValidator
				.validateRoles(Set.of("task-read"), Set.of("task-read"));
		
		AudienceValidationResult audienceValidation = KeycloakConfigValidator
				.validateAudience(Set.of("pragma-backend"), "pragma-backend");
		
		NamingConsistencyResult namingConsistency = KeycloakConfigValidator
				.validateRoleNamingConsistency(Set.of("task-read", "task-write"));
		
		TokenLifetimeValidationResult tokenLifetime = KeycloakConfigValidator
				.validateTokenLifetime(300, 1800);
		
		ValidationReport report = ValidationReport.builder()
				.roleValidation(roleValidation)
				.audienceValidation(audienceValidation)
				.namingConsistency(namingConsistency)
				.tokenLifetime(tokenLifetime)
				.build();
		
		assertThat(report.fullyValid()).isTrue();
		
		String detailedReport = report.detailedReport();
		assertThat(detailedReport).isNotNull();
		assertThat(detailedReport).contains("Overall Status");
		assertThat(detailedReport).contains("VALID");
	}

	@Test
	void testValidationReport_WithIssues()
	{
		RoleValidationResult roleValidation = KeycloakConfigValidator
				.validateRoles(Set.of("task-read"), Set.of("task-read", "task-write")); // Missing role
		
		AudienceValidationResult audienceValidation = KeycloakConfigValidator
				.validateAudience(Set.of("account"), "pragma-backend"); // Wrong audience
		
		NamingConsistencyResult namingConsistency = KeycloakConfigValidator
				.validateRoleNamingConsistency(Set.of("task-read"));
		
		TokenLifetimeValidationResult tokenLifetime = KeycloakConfigValidator
				.validateTokenLifetime(30, 60); // Too short
		
		ValidationReport report = ValidationReport.builder()
				.roleValidation(roleValidation)
				.audienceValidation(audienceValidation)
				.namingConsistency(namingConsistency)
				.tokenLifetime(tokenLifetime)
				.build();
		
		assertThat(report.fullyValid()).isFalse();

		String detailedReport = report.detailedReport();
		assertThat(detailedReport).isNotNull();
		assertThat(detailedReport).contains("ISSUES DETECTED");
		assertThat(detailedReport).contains("task-write"); // Missing role
	}
}