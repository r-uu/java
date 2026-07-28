package de.ruu.app.pragma.fx.task.graph;

import de.ruu.app.pragma.bean.TaskBean;
import de.ruu.app.pragma.bean.TaskGroupBean;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GraphTimelineTest
{
  @Test
  void createScaleUsesFallbackRangeWhenNoDates()
  {
    TaskBean t = persistedTask(1L, "A");
    GraphTimeline.Scale scale = GraphTimeline.createScale(
        List.of(t), GraphTimeline.Granularity.WEEK, 20, 28, 68);

    assertThat(scale.maxDate()).isAfter(scale.minDate());
  }

  @Test
  void createScaleHonorsGranularityDayWidth()
  {
    TaskBean t = persistedTask(1L, "A");
    t.scheduledStart(LocalDate.of(2026, 1, 1));
    t.scheduledFinish(LocalDate.of(2026, 1, 10));

    GraphTimeline.Scale day = GraphTimeline.createScale(List.of(t), GraphTimeline.Granularity.DAY, 20, 28, 68);
    GraphTimeline.Scale week = GraphTimeline.createScale(List.of(t), GraphTimeline.Granularity.WEEK, 20, 28, 68);
    GraphTimeline.Scale month = GraphTimeline.createScale(List.of(t), GraphTimeline.Granularity.MONTH, 20, 28, 68);

    assertThat(day.dayWidth()).isGreaterThan(week.dayWidth());
    assertThat(week.dayWidth()).isGreaterThan(month.dayWidth());
  }

  @Test
  void dateXIncreasesWithDate()
  {
    TaskBean t = persistedTask(1L, "A");
    t.scheduledStart(LocalDate.of(2026, 1, 1));
    t.scheduledFinish(LocalDate.of(2026, 1, 31));
    GraphTimeline.Scale scale = GraphTimeline.createScale(
        List.of(t), GraphTimeline.Granularity.DAY, 20, 28, 68);

    double x1 = GraphTimeline.dateX(LocalDate.of(2026, 1, 5), scale);
    double x2 = GraphTimeline.dateX(LocalDate.of(2026, 1, 10), scale);

    assertThat(x2).isGreaterThan(x1);
  }

  private static TaskBean persistedTask(Long id, String name)
  {
    TaskBean task = new TaskBean(new TaskGroupBean("G"), name);
    try
    {
      Field idField = TaskBean.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(task, id);
    }
    catch (ReflectiveOperationException e)
    {
      throw new IllegalStateException("Failed to create persisted test task.", e);
    }
    return task;
  }
}
