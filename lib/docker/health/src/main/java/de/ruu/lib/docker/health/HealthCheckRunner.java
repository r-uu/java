package de.ruu.lib.docker.health;

import de.ruu.lib.docker.health.check.HealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Runs multiple health checks and aggregates results.
 *
 * <p>This is the main entry point for performing health checks.
 * It can be configured with any combination of health checks and
 * provides formatted output with fix suggestions.
 *
 * <p><b>Example usage:</b>
 * <pre>
 * HealthCheckRunner runner = HealthCheckRunner.builder()
 *     .addCheck(new DockerDaemonHealthCheck())
 *     .addCheck(new PostgresDatabaseHealthCheck("postgres", "pragma", 5432))
 *     .addCheck(new KeycloakServerHealthCheck())
 *     .build();
 *
 * HealthCheckRunner.RunResult result = runner.runAll();
 * if (!result.healthy()) {
 *     result.failures().forEach(f -> System.err.println(f.service()));
 * }
 * </pre>
 */
public class HealthCheckRunner
{
  private static final Logger log = LoggerFactory.getLogger(HealthCheckRunner.class);

  private final List<HealthCheck> checks;

  private HealthCheckRunner(List<HealthCheck> checks)
  {
    this.checks = new ArrayList<>(checks);
  }

  /**
   * Runs all configured health checks and returns an immutable result.
   * <p>
   * This method is stateless and safe for concurrent use: each call creates
   * fresh local state and returns it as a {@link RunResult} value object.
   *
   * @return aggregated result of all health checks
   */
  public RunResult runAll()
  {
    List<HealthCheckResult> results = new ArrayList<>();
    boolean allHealthy = true;

    log.info("""
        ================================================================
        Docker Environment Health Check
        ================================================================""");

    for (HealthCheck check : checks)
    {
      HealthCheckResult result = check.check();
      results.add(result);
      if (!result.healthy()) allHealthy = false;
    }

    RunResult runResult = new RunResult(allHealthy, List.copyOf(results));
    printResults(runResult);
    return runResult;
  }

  private void printResults(RunResult runResult)
  {
    log.info("================================================================");

    if (runResult.healthy())
    {
      log.info("""
          [OK] ALL SERVICES HEALTHY - Ready to start!
          ================================================================""");
    }
    else
    {
      List<HealthCheckResult> failures = runResult.failures();
      log.error("[FAIL] HEALTH CHECK FAILED - {} issue(s) found", failures.size());
      log.info("""
          ================================================================

          HOW TO FIX:
          """);

      for (int i = 0; i < failures.size(); i++)
      {
        HealthCheckResult failure = failures.get(i);
        log.info("""
            Issue {}/{}: {}
              Problem: {}

              Quick fix (alias):
                 {}

              Full command:
                 {}
            """,
            i + 1, failures.size(), failure.service(),
            failure.problem(),
            failure.alias(),
            failure.fixCommand());
      }

      log.info("""
          ================================================================
          TIP: Copy & paste commands above to fix all issues
          ================================================================""");
    }
  }

  /**
   * Creates a new builder for configuring health checks.
   */
  public static Builder builder()
  {
    return new Builder();
  }

  /**
   * Immutable value object holding the aggregated result of a {@link #runAll()} call.
   */
  public record RunResult(boolean allHealthy, List<HealthCheckResult> results)
  {
    /** @return {@code true} if all health checks passed */
    public boolean healthy() { return allHealthy; }

    /** @return list of failed health check results */
    public List<HealthCheckResult> failures()
    {
      return results.stream().filter(r -> !r.healthy()).toList();
    }
  }

  /**
   * Builder for HealthCheckRunner.
   */
  public static class Builder
  {
    private final List<HealthCheck> checks = new ArrayList<>();

    /** Adds a single health check. */
    public Builder addCheck(HealthCheck check)
    {
      checks.add(check);
      return this;
    }

    /** Adds multiple health checks. */
    public Builder addChecks(HealthCheck... checks)
    {
      this.checks.addAll(Arrays.asList(checks));
      return this;
    }

    /** Adds multiple health checks from a list. */
    public Builder addChecks(List<HealthCheck> checks)
    {
      this.checks.addAll(checks);
      return this;
    }

    /** Builds the HealthCheckRunner. */
    public HealthCheckRunner build()
    {
      return new HealthCheckRunner(checks);
    }
  }
}
