package de.ruu.lib.docker.health;

import de.ruu.lib.docker.health.check.HealthCheck;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HealthCheckRunnerTest
{
  private static HealthCheck passing(String name)
  {
    return new HealthCheck()
    {
      @Override public String getName()          { return name; }
      @Override public HealthCheckResult check() { return HealthCheckResult.success(name); }
    };
  }

  private static HealthCheck failing(String name, String problem)
  {
    return new HealthCheck()
    {
      @Override public String getName()          { return name; }
      @Override public HealthCheckResult check() { return HealthCheckResult.failure(name, problem, "fix", "alias"); }
    };
  }

  @Test
  void allPassingChecksYieldHealthyResult()
  {
    HealthCheckRunner runner = HealthCheckRunner.builder()
        .addCheck(passing("A"))
        .addCheck(passing("B"))
        .build();

    HealthCheckRunner.RunResult result = runner.runAll();

    assertThat(result.healthy()).isTrue();
    assertThat(result.failures()).isEmpty();
    assertThat(result.results()).hasSize(2);
  }

  @Test
  void oneFailingCheckYieldsUnhealthyResult()
  {
    HealthCheckRunner runner = HealthCheckRunner.builder()
        .addCheck(passing("A"))
        .addCheck(failing("B", "B is down"))
        .build();

    HealthCheckRunner.RunResult result = runner.runAll();

    assertThat(result.healthy()).isFalse();
    assertThat(result.failures()).hasSize(1);
    assertThat(result.failures().get(0).service()).isEqualTo("B");
  }

  @Test
  void allFailingChecksReportedInFailures()
  {
    HealthCheckRunner runner = HealthCheckRunner.builder()
        .addChecks(failing("X", "x broken"), failing("Y", "y broken"))
        .build();

    HealthCheckRunner.RunResult result = runner.runAll();

    assertThat(result.healthy()).isFalse();
    assertThat(result.failures()).hasSize(2);
  }

  @Test
  void emptyRunnerYieldsHealthyResult()
  {
    HealthCheckRunner runner = HealthCheckRunner.builder().build();

    HealthCheckRunner.RunResult result = runner.runAll();

    assertThat(result.healthy()).isTrue();
    assertThat(result.results()).isEmpty();
  }

  @Test
  void runResultIsImmutable()
  {
    HealthCheckRunner runner = HealthCheckRunner.builder()
        .addCheck(passing("A"))
        .build();

    HealthCheckRunner.RunResult result = runner.runAll();

    assertThat(result.results()).isUnmodifiable();
  }

  @Test
  void healthyAliasesAllHealthy()
  {
    HealthCheckRunner runner = HealthCheckRunner.builder()
        .addCheck(failing("F", "problem"))
        .build();

    HealthCheckRunner.RunResult result = runner.runAll();

    assertThat(result.healthy()).isEqualTo(result.allHealthy());
  }
}
