package de.ruu.app.pragma.fx.task.view;

import de.ruu.app.pragma.bean.TaskBean;
import de.ruu.app.pragma.bean.TaskGroupBean;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TaskRelationRulesTest
{
  @Test
  void detectsCycleWhenPathAlreadyExists()
  {
    TaskBean a = persistedTask(1L, "A");
    TaskBean b = persistedTask(2L, "B");
    TaskBean c = persistedTask(3L, "C");

    a.addSuccessor(b);
    b.addSuccessor(c);

    Map<Long, TaskBean> byId = Map.of(1L, a, 2L, b, 3L, c);
    boolean cyclic = TaskRelationRules.wouldCreateCycle(3L, 1L, id -> Optional.ofNullable(byId.get(id)));

    assertThat(cyclic).isTrue();
  }

  @Test
  void allowsAcyclicRelation()
  {
    TaskBean a = persistedTask(1L, "A");
    TaskBean b = persistedTask(2L, "B");
    TaskBean c = persistedTask(3L, "C");

    a.addSuccessor(b);

    Map<Long, TaskBean> byId = Map.of(1L, a, 2L, b, 3L, c);
    boolean cyclic = TaskRelationRules.wouldCreateCycle(1L, 3L, id -> Optional.ofNullable(byId.get(id)));

    assertThat(cyclic).isFalse();
  }

  @Test
  void selfRelationIsAlwaysCycle()
  {
    TaskBean a = persistedTask(1L, "A");
    boolean cyclic = TaskRelationRules.wouldCreateCycle(1L, 1L, id -> Optional.of(a));
    assertThat(cyclic).isTrue();
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
