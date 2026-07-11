package de.ruu.lib.keycloak.admin.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Validates consistency between Keycloak configuration, backend REST APIs, and client REST calls.
 *
 * <p>This utility helps identify common configuration issues such as:
 * <ul>
 *   <li>Missing roles in Keycloak that are required by backend @RolesAllowed annotations</li>
 *   <li>Audience mismatches between JWT tokens and backend configuration</li>
 *   <li>Role naming inconsistencies (e.g., "taskgroup-read" vs "task-group-read")</li>
 *   <li>Token lifetime configuration issues</li>
 * </ul>
 *
 * @author r-uu
 * @since 2025-12-27
 */
public class KeycloakConfigValidator
{
	private static final Logger log = LoggerFactory.getLogger(KeycloakConfigValidator.class);

	/**
	 * Validates that all required roles exist in the Keycloak token.
	 *
	 * @param tokenRoles roles extracted from JWT token (realm_access.roles claim)
	 * @param requiredRoles roles required by the backend (from @RolesAllowed annotations)
	 * @return validation result with any missing roles
	 */
	public static RoleValidationResult validateRoles(Set<String> tokenRoles, Set<String> requiredRoles)
	{
		Set<String> missingRoles = new HashSet<>(requiredRoles);
		missingRoles.removeAll(tokenRoles);

		Set<String> extraRoles = new HashSet<>(tokenRoles);
		extraRoles.removeAll(requiredRoles);

		boolean valid = missingRoles.isEmpty();

		return RoleValidationResult.builder()
				.valid(valid)
				.tokenRoles(tokenRoles)
				.requiredRoles(requiredRoles)
				.missingRoles(missingRoles)
				.extraRoles(extraRoles)
				.build();
	}

	/**
	 * Validates that the token audience matches the expected backend audience.
	 *
	 * @param tokenAudiences audiences from JWT token (aud claim)
	 * @param expectedAudience audience configured in backend server.xml
	 * @return validation result
	 */
	public static AudienceValidationResult validateAudience(
			Set<String> tokenAudiences,
			String expectedAudience)
	{
		boolean valid = tokenAudiences.contains(expectedAudience);

		return AudienceValidationResult.builder()
				.valid(valid)
				.tokenAudiences(tokenAudiences)
				.expectedAudience(expectedAudience)
				.build();
	}

	/**
	 * Detects potential role naming inconsistencies.
	 *
	 * <p>Common issues:
	 * <ul>
	 *   <li>Keycloak uses "task-group-read" but backend uses "taskgroup-read"</li>
	 *   <li>Mixed conventions within the same application</li>
	 * </ul>
	 *
	 * @param roles all roles to check
	 * @return detected naming issues
	 */
	public static NamingConsistencyResult validateRoleNamingConsistency(Set<String> roles)
	{
		List<String> dashRoles = roles.stream()
				.filter(r -> r.contains("-") && !r.startsWith("default-"))
				.collect(Collectors.toList());

		List<String> suspiciousRoles = new ArrayList<>();
		Map<String, String> suggestions = new HashMap<>();

		for (String role : roles)
		{
			// Check if a similar role with dashes exists
			for (String dashRole : dashRoles)
			{
				String normalized = dashRole.replace("-", "");
				if (role.replace("-", "").equalsIgnoreCase(normalized) && !role.equals(dashRole))
				{
					suspiciousRoles.add(role);
					suggestions.put(role, dashRole);
				}
			}
		}

		return NamingConsistencyResult.builder()
				.consistent(suspiciousRoles.isEmpty())
				.suspiciousRoles(suspiciousRoles)
				.suggestions(suggestions)
				.build();
	}

	/**
	 * Validates token lifetime settings.
	 *
	 * @param accessTokenLifetimeSeconds access token lifetime in seconds
	 * @param refreshTokenLifetimeSeconds refresh token lifetime in seconds
	 * @return validation result with warnings for potentially problematic values
	 */
	public static TokenLifetimeValidationResult validateTokenLifetime(
			int accessTokenLifetimeSeconds,
			int refreshTokenLifetimeSeconds)
	{
		List<String> warnings = new ArrayList<>();

		// Access token too short (less than 1 minute)
		if (accessTokenLifetimeSeconds < 60)
		{
			warnings.add(String.format(
					"Access token lifetime very short (%ds). May cause frequent re-authentication.",
					accessTokenLifetimeSeconds));
		}

		// Access token too long (more than 30 minutes)
		if (accessTokenLifetimeSeconds > 1800)
		{
			warnings.add(String.format(
					"Access token lifetime long (%ds = %d min). Consider shorter lifetime for better security.",
					accessTokenLifetimeSeconds, accessTokenLifetimeSeconds / 60));
		}

		// Refresh token shorter than access token
		if (refreshTokenLifetimeSeconds < accessTokenLifetimeSeconds)
		{
			warnings.add(String.format(
					"Refresh token lifetime (%ds) shorter than access token (%ds). This is unusual.",
					refreshTokenLifetimeSeconds, accessTokenLifetimeSeconds));
		}

		// Refresh token too short (less than 5 minutes)
		if (refreshTokenLifetimeSeconds < 300)
		{
			warnings.add(String.format(
					"Refresh token lifetime short (%ds). May cause session interruptions.",
					refreshTokenLifetimeSeconds));
		}

		return TokenLifetimeValidationResult.builder()
				.accessTokenLifetimeSeconds(accessTokenLifetimeSeconds)
				.refreshTokenLifetimeSeconds(refreshTokenLifetimeSeconds)
				.warnings(warnings)
				.build();
	}

	// ===== Result Classes =====

	public static class RoleValidationResult
	{
		private final boolean     valid;
		private final Set<String> tokenRoles;
		private final Set<String> requiredRoles;
		private final Set<String> missingRoles;
		private final Set<String> extraRoles;

		private RoleValidationResult(
				boolean     valid,
				Set<String> tokenRoles,
				Set<String> requiredRoles,
				Set<String> missingRoles,
				Set<String> extraRoles)
		{
			this.valid         = valid;
			this.tokenRoles    = tokenRoles;
			this.requiredRoles = requiredRoles;
			this.missingRoles  = missingRoles;
			this.extraRoles    = extraRoles;
		}

		public static Builder builder() { return new Builder(); }

		public static class Builder
		{
			private boolean     valid;
			private Set<String> tokenRoles;
			private Set<String> requiredRoles;
			private Set<String> missingRoles;
			private Set<String> extraRoles;

			public Builder valid        (boolean     v) { this.valid         = v; return this; }
			public Builder tokenRoles   (Set<String> v) { this.tokenRoles    = v; return this; }
			public Builder requiredRoles(Set<String> v) { this.requiredRoles = v; return this; }
			public Builder missingRoles (Set<String> v) { this.missingRoles  = v; return this; }
			public Builder extraRoles   (Set<String> v) { this.extraRoles    = v; return this; }

			public RoleValidationResult build()
			{
				return new RoleValidationResult(valid, tokenRoles, requiredRoles, missingRoles, extraRoles);
			}
		}

		public boolean     valid()         { return valid; }
		public Set<String> tokenRoles()    { return tokenRoles; }
		public Set<String> requiredRoles() { return requiredRoles; }
		public Set<String> missingRoles()  { return missingRoles; }
		public Set<String> extraRoles()    { return extraRoles; }

		public String summary()
		{
			if (valid)
			{
				return "✓ All required roles are present in token";
			}
			else
			{
				return String.format("✗ Missing %d required role(s): %s",
						missingRoles.size(),
						String.join(", ", missingRoles));
			}
		}

		public List<String> recommendations()
		{
			if (valid) return List.of();

			List<String> recommendations = new ArrayList<>();
			recommendations.add("Add missing roles in Keycloak Admin Console:");
			for (String role : missingRoles)
			{
				recommendations.add(String.format(
						"  docker exec keycloak /opt/keycloak/bin/kcadm.sh create roles " +
						"-r pragma-realm -s name=%s", role));
			}
			return recommendations;
		}

		@Override public boolean equals(Object o)
		{
			if (this == o) return true;
			if (!(o instanceof RoleValidationResult other)) return false;
			return valid == other.valid
				&& Objects.equals(tokenRoles,    other.tokenRoles)
				&& Objects.equals(requiredRoles, other.requiredRoles)
				&& Objects.equals(missingRoles,  other.missingRoles)
				&& Objects.equals(extraRoles,    other.extraRoles);
		}

		@Override public int hashCode()
		{
			return Objects.hash(valid, tokenRoles, requiredRoles, missingRoles, extraRoles);
		}

		@Override public String toString()
		{
			return "RoleValidationResult(valid=" + valid
				+ ", missingRoles=" + missingRoles + ")";
		}
	}

	public static class AudienceValidationResult
	{
		private final boolean     valid;
		private final Set<String> tokenAudiences;
		private final String      expectedAudience;

		private AudienceValidationResult(boolean valid, Set<String> tokenAudiences, String expectedAudience)
		{
			this.valid            = valid;
			this.tokenAudiences   = tokenAudiences;
			this.expectedAudience = expectedAudience;
		}

		public static Builder builder() { return new Builder(); }

		public static class Builder
		{
			private boolean     valid;
			private Set<String> tokenAudiences;
			private String      expectedAudience;

			public Builder valid           (boolean     v) { this.valid            = v; return this; }
			public Builder tokenAudiences  (Set<String> v) { this.tokenAudiences   = v; return this; }
			public Builder expectedAudience(String      v) { this.expectedAudience = v; return this; }

			public AudienceValidationResult build()
			{
				return new AudienceValidationResult(valid, tokenAudiences, expectedAudience);
			}
		}

		public boolean     valid()            { return valid; }
		public Set<String> tokenAudiences()   { return tokenAudiences; }
		public String      expectedAudience() { return expectedAudience; }

		public String summary()
		{
			if (valid)
			{
				return String.format("✓ Token audience contains expected value '%s'", expectedAudience);
			}
			else
			{
				return String.format("✗ Token audience %s does not contain expected '%s'",
						tokenAudiences, expectedAudience);
			}
		}

		public List<String> recommendations()
		{
			if (valid) return List.of();

			List<String> recommendations = new ArrayList<>();
			recommendations.add("Configure audience mapper in Keycloak:");
			recommendations.add(String.format(
					"  1. Navigate to: Clients → pragma-frontend → Client scopes → pragma-frontend-dedicated"));
			recommendations.add("  2. Add mapper → By configuration → Audience");
			recommendations.add(String.format("  3. Set 'Included Custom Audience' = %s", expectedAudience));
			return recommendations;
		}

		@Override public boolean equals(Object o)
		{
			if (this == o) return true;
			if (!(o instanceof AudienceValidationResult other)) return false;
			return valid == other.valid
				&& Objects.equals(tokenAudiences,   other.tokenAudiences)
				&& Objects.equals(expectedAudience, other.expectedAudience);
		}

		@Override public int hashCode() { return Objects.hash(valid, tokenAudiences, expectedAudience); }

		@Override public String toString()
		{
			return "AudienceValidationResult(valid=" + valid
				+ ", expectedAudience=" + expectedAudience + ")";
		}
	}

	public static class NamingConsistencyResult
	{
		private final boolean             consistent;
		private final List<String>        suspiciousRoles;
		private final Map<String, String> suggestions;

		private NamingConsistencyResult(
				boolean             consistent,
				List<String>        suspiciousRoles,
				Map<String, String> suggestions)
		{
			this.consistent      = consistent;
			this.suspiciousRoles = suspiciousRoles;
			this.suggestions     = suggestions;
		}

		public static Builder builder() { return new Builder(); }

		public static class Builder
		{
			private boolean             consistent;
			private List<String>        suspiciousRoles;
			private Map<String, String> suggestions;

			public Builder consistent     (boolean             v) { this.consistent      = v; return this; }
			public Builder suspiciousRoles(List<String>        v) { this.suspiciousRoles = v; return this; }
			public Builder suggestions    (Map<String, String> v) { this.suggestions     = v; return this; }

			public NamingConsistencyResult build()
			{
				return new NamingConsistencyResult(consistent, suspiciousRoles, suggestions);
			}
		}

		public boolean             consistent()      { return consistent; }
		public List<String>        suspiciousRoles() { return suspiciousRoles; }
		public Map<String, String> suggestions()     { return suggestions; }

		public String summary()
		{
			if (consistent)
			{
				return "✓ Role naming is consistent";
			}
			else
			{
				return String.format("⚠ Detected %d potential naming inconsistenc%s",
						suspiciousRoles.size(),
						suspiciousRoles.size() == 1 ? "y" : "ies");
			}
		}

		public List<String> recommendations()
		{
			if (consistent) return List.of();

			List<String> recommendations = new ArrayList<>();
			recommendations.add("Consider standardizing role names:");
			for (Map.Entry<String, String> entry : suggestions.entrySet())
			{
				recommendations.add(String.format("  '%s' → '%s'", entry.getKey(), entry.getValue()));
			}
			return recommendations;
		}

		@Override public boolean equals(Object o)
		{
			if (this == o) return true;
			if (!(o instanceof NamingConsistencyResult other)) return false;
			return consistent == other.consistent
				&& Objects.equals(suspiciousRoles, other.suspiciousRoles)
				&& Objects.equals(suggestions,     other.suggestions);
		}

		@Override public int hashCode() { return Objects.hash(consistent, suspiciousRoles, suggestions); }

		@Override public String toString()
		{
			return "NamingConsistencyResult(consistent=" + consistent
				+ ", suspiciousRoles=" + suspiciousRoles + ")";
		}
	}

	public static class TokenLifetimeValidationResult
	{
		private final int        accessTokenLifetimeSeconds;
		private final int        refreshTokenLifetimeSeconds;
		private final List<String> warnings;

		private TokenLifetimeValidationResult(
				int          accessTokenLifetimeSeconds,
				int          refreshTokenLifetimeSeconds,
				List<String> warnings)
		{
			this.accessTokenLifetimeSeconds  = accessTokenLifetimeSeconds;
			this.refreshTokenLifetimeSeconds = refreshTokenLifetimeSeconds;
			this.warnings                    = warnings;
		}

		public static Builder builder() { return new Builder(); }

		public static class Builder
		{
			private int          accessTokenLifetimeSeconds;
			private int          refreshTokenLifetimeSeconds;
			private List<String> warnings;

			public Builder accessTokenLifetimeSeconds (int          v) { this.accessTokenLifetimeSeconds  = v; return this; }
			public Builder refreshTokenLifetimeSeconds(int          v) { this.refreshTokenLifetimeSeconds = v; return this; }
			public Builder warnings                   (List<String> v) { this.warnings                   = v; return this; }

			public TokenLifetimeValidationResult build()
			{
				return new TokenLifetimeValidationResult(
						accessTokenLifetimeSeconds, refreshTokenLifetimeSeconds, warnings);
			}
		}

		public int          accessTokenLifetimeSeconds()  { return accessTokenLifetimeSeconds; }
		public int          refreshTokenLifetimeSeconds() { return refreshTokenLifetimeSeconds; }
		public List<String> warnings()                    { return warnings; }

		public String summary()
		{
			if (warnings.isEmpty())
			{
				return String.format("✓ Token lifetimes configured (access=%ds, refresh=%ds)",
						accessTokenLifetimeSeconds, refreshTokenLifetimeSeconds);
			}
			else
			{
				return String.format("⚠ Token lifetime has %d warning(s)", warnings.size());
			}
		}

		public List<String> recommendations()
		{
			if (warnings.isEmpty()) return List.of();

			List<String> recommendations = new ArrayList<>();
			recommendations.add("Review token lifetime configuration in Keycloak:");
			recommendations.add("  Realm Settings → Tokens → Access Token Lifespan");
			recommendations.add("  Realm Settings → Tokens → Refresh Token Lifespan");
			return recommendations;
		}

		@Override public boolean equals(Object o)
		{
			if (this == o) return true;
			if (!(o instanceof TokenLifetimeValidationResult other)) return false;
			return accessTokenLifetimeSeconds  == other.accessTokenLifetimeSeconds
				&& refreshTokenLifetimeSeconds == other.refreshTokenLifetimeSeconds
				&& Objects.equals(warnings, other.warnings);
		}

		@Override public int hashCode()
		{
			return Objects.hash(accessTokenLifetimeSeconds, refreshTokenLifetimeSeconds, warnings);
		}

		@Override public String toString()
		{
			return "TokenLifetimeValidationResult(access=" + accessTokenLifetimeSeconds
				+ "s, refresh=" + refreshTokenLifetimeSeconds + "s, warnings=" + warnings + ")";
		}
	}

	/**
	 * Comprehensive validation result combining all checks.
	 */
	public static class ValidationReport
	{
		private final RoleValidationResult          roleValidation;
		private final AudienceValidationResult      audienceValidation;
		private final NamingConsistencyResult       namingConsistency;
		private final TokenLifetimeValidationResult tokenLifetime;

		private ValidationReport(
				RoleValidationResult          roleValidation,
				AudienceValidationResult      audienceValidation,
				NamingConsistencyResult       namingConsistency,
				TokenLifetimeValidationResult tokenLifetime)
		{
			this.roleValidation     = roleValidation;
			this.audienceValidation = audienceValidation;
			this.namingConsistency  = namingConsistency;
			this.tokenLifetime      = tokenLifetime;
		}

		public static Builder builder() { return new Builder(); }

		public static class Builder
		{
			private RoleValidationResult          roleValidation;
			private AudienceValidationResult      audienceValidation;
			private NamingConsistencyResult       namingConsistency;
			private TokenLifetimeValidationResult tokenLifetime;

			public Builder roleValidation    (RoleValidationResult          v) { this.roleValidation     = v; return this; }
			public Builder audienceValidation(AudienceValidationResult      v) { this.audienceValidation = v; return this; }
			public Builder namingConsistency (NamingConsistencyResult       v) { this.namingConsistency  = v; return this; }
			public Builder tokenLifetime     (TokenLifetimeValidationResult v) { this.tokenLifetime      = v; return this; }

			public ValidationReport build()
			{
				return new ValidationReport(roleValidation, audienceValidation, namingConsistency, tokenLifetime);
			}
		}

		public RoleValidationResult          roleValidation()     { return roleValidation; }
		public AudienceValidationResult      audienceValidation() { return audienceValidation; }
		public NamingConsistencyResult       namingConsistency()  { return namingConsistency; }
		public TokenLifetimeValidationResult tokenLifetime()      { return tokenLifetime; }

		public boolean fullyValid()
		{
			return roleValidation.valid()
					&& audienceValidation.valid()
					&& namingConsistency.consistent()
					&& (tokenLifetime == null || tokenLifetime.warnings().isEmpty());
		}

		public String detailedReport()
		{
			StringBuilder report = new StringBuilder();
			report.append("Keycloak Configuration Validation Report\n");
			report.append("=".repeat(60)).append("\n\n");

			report.append("Role Validation:\n");
			report.append("  ").append(roleValidation.summary()).append("\n");
			if (!roleValidation.valid())
			{
				roleValidation.recommendations().forEach(r -> report.append("  ").append(r).append("\n"));
			}
			report.append("\n");

			report.append("Audience Validation:\n");
			report.append("  ").append(audienceValidation.summary()).append("\n");
			if (!audienceValidation.valid())
			{
				audienceValidation.recommendations().forEach(r -> report.append("  ").append(r).append("\n"));
			}
			report.append("\n");

			report.append("Role Naming Consistency:\n");
			report.append("  ").append(namingConsistency.summary()).append("\n");
			if (!namingConsistency.consistent())
			{
				namingConsistency.recommendations().forEach(r -> report.append("  ").append(r).append("\n"));
			}
			report.append("\n");

			if (tokenLifetime != null)
			{
				report.append("Token Lifetime:\n");
				report.append("  ").append(tokenLifetime.summary()).append("\n");
				if (!tokenLifetime.warnings().isEmpty())
				{
					tokenLifetime.warnings().forEach(w -> report.append("  ⚠ ").append(w).append("\n"));
					tokenLifetime.recommendations().forEach(r -> report.append("  ").append(r).append("\n"));
				}
			}

			report.append("\n");
			report.append("=".repeat(60)).append("\n");
			report.append("Overall Status: ").append(fullyValid() ? "✓ VALID" : "✗ ISSUES DETECTED").append("\n");

			return report.toString();
		}

		@Override public boolean equals(Object o)
		{
			if (this == o) return true;
			if (!(o instanceof ValidationReport other)) return false;
			return Objects.equals(roleValidation,     other.roleValidation)
				&& Objects.equals(audienceValidation, other.audienceValidation)
				&& Objects.equals(namingConsistency,  other.namingConsistency)
				&& Objects.equals(tokenLifetime,      other.tokenLifetime);
		}

		@Override public int hashCode()
		{
			return Objects.hash(roleValidation, audienceValidation, namingConsistency, tokenLifetime);
		}
	}
}
