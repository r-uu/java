package de.ruu.app.pragma.fx.task.view;

import de.ruu.app.pragma.bean.TaskBean;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

final class TaskRelationRules
{
  private TaskRelationRules() { }

  /**
   * Checks whether adding edge predecessor -> successor would create a cycle.
   */
  static boolean wouldCreateCycle(Long predecessorId, Long successorId, Function<Long, Optional<TaskBean>> taskProvider)
  {
    if (predecessorId == null || successorId == null) return false;
    if (predecessorId.equals(successorId)) return true;

    Set<Long> visited = new HashSet<>();
    ArrayDeque<Long> stack = new ArrayDeque<>();
    stack.push(successorId);

    while (!stack.isEmpty())
    {
      Long currentId = stack.pop();
      if (!visited.add(currentId)) continue;
      if (currentId.equals(predecessorId)) return true;
      taskProvider.apply(currentId)
          .flatMap(TaskBean::successors)
          .orElse(Set.of())
          .stream()
          .map(TaskBean::id)
          .filter(id -> id != null && !visited.contains(id))
          .forEach(stack::push);
    }
    return false;
  }
}
