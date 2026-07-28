package de.ruu.app.pragma.fx.task.graph;

import de.ruu.app.pragma.bean.TaskBean;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Stateless time-axis helpers for graph rendering.
 */
public final class GraphTimeline
{
  private GraphTimeline() { }

  public enum Granularity
  {
    DAY(12.0),
    WEEK(4.0),
    MONTH(1.6);

    private final double dayWidth;

    Granularity(double dayWidth) { this.dayWidth = dayWidth; }
    public double dayWidth() { return dayWidth; }
  }

  public static Scale createScale(
      Collection<TaskBean> tasks,
      Granularity granularity,
      double axisStartX,
      double axisY,
      double tasksTopY)
  {
    LocalDate now = LocalDate.now();
    LocalDate minDate = tasks.stream()
        .flatMap(task -> Stream.of(task.scheduledStart().orElse(null), task.scheduledFinish().orElse(null)))
        .filter(Objects::nonNull)
        .min(LocalDate::compareTo)
        .orElse(now);
    LocalDate maxDate = tasks.stream()
        .flatMap(task -> Stream.of(task.scheduledStart().orElse(null), task.scheduledFinish().orElse(null)))
        .filter(Objects::nonNull)
        .max(LocalDate::compareTo)
        .orElse(minDate.plusDays(30));
    if (!maxDate.isAfter(minDate)) maxDate = minDate.plusDays(1);
    return new Scale(minDate, maxDate, granularity.dayWidth(), axisStartX, axisY, tasksTopY);
  }

  public static double dateX(LocalDate date, Scale scale)
  {
    long days = Math.max(0, ChronoUnit.DAYS.between(scale.minDate(), date));
    return scale.axisStartX() + days * scale.dayWidth();
  }

  public record Scale(
      LocalDate minDate,
      LocalDate maxDate,
      double dayWidth,
      double axisStartX,
      double axisY,
      double tasksTopY) { }
}
