package de.ruu.app.pragma.bean;

import de.ruu.app.pragma.core.PersistentTask;
import de.ruu.app.pragma.core.PersistentTaskGroup;
import de.ruu.app.pragma.core.TaskPriority;
import de.ruu.app.pragma.core.TaskStatus;
import de.ruu.app.pragma.dto.TaskDto;
import de.ruu.app.pragma.dto.TaskGroupDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MappingsTest
{
    // ── fixtures ───────────────────────────────────────────────────────────

    private TaskGroupBean groupBean;
    private TaskBean      taskA;
    private TaskBean      taskB;

    @BeforeEach
    void setUp()
    {
        groupBean = new TaskGroupBean("Sprint 1");
        taskA     = new TaskBean(groupBean, "Task A");
        taskB     = new TaskBean(groupBean, "Task B");
    }

    // ── TaskGroupBean → TaskGroupDto ───────────────────────────────────────

    @Test void groupBean_toDto_scalarFields()
    {
        TaskGroupDto dto = Mappings.toDto(groupBean);

        assertThat(dto.name()).isEqualTo("Sprint 1");
        assertThat(dto.id())     .isNull(); // not set on fresh bean
        assertThat(dto.version()).isNull();
    }

    @Test void groupBean_toDto_idAndVersionPropagated()
    {
        TaskGroupDto in  = persistedGroupDto(7L, (short) 3, "G");
        TaskGroupBean bean = new TaskGroupBean(in);
        TaskGroupDto  out  = Mappings.toDto(bean);

        assertThat(out.id())     .isEqualTo(7L);
        assertThat(out.version()).isEqualTo((short) 3);
        assertThat(out.name())   .isEqualTo("G");
    }

    @Test void groupBean_toDto_doesNotIncludeUnloadedTasks()
    {
        TaskGroupDto dto = Mappings.toDto(groupBean);
        assertThat(dto.tasks()).isEmpty();
    }

    // ── TaskGroupDto → TaskGroupBean ───────────────────────────────────────

    @Test void groupDto_toBean_scalarFields()
    {
        TaskGroupDto  dto  = persistedGroupDto(42L, (short) 1, "Sprint 2");
        TaskGroupBean bean = Mappings.toBean(dto);

        assertThat(bean.name())   .isEqualTo("Sprint 2");
        assertThat(bean.id())     .isEqualTo(42L);
        assertThat(bean.version()).isEqualTo((short) 1);
    }

    // ── TaskBean → TaskDto ─────────────────────────────────────────────────

    @Test void taskBean_toDto_scalarFields()
    {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end   = LocalDate.of(2026, 1, 31);
        taskA.description("desc").scheduledStart(start).scheduledFinish(end).status(TaskStatus.CLOSED);
        taskA.priority(TaskPriority.HIGH);

        TaskDto dto = Mappings.toDto(taskA);

        assertThat(dto.name())        .isEqualTo("Task A");
        assertThat(dto.description()) .hasValue("desc");
        assertThat(dto.scheduledStart()).hasValue(start);
        assertThat(dto.scheduledFinish())  .hasValue(end);
        assertThat(dto.status())      .isEqualTo(TaskStatus.CLOSED);
        assertThat(dto.priority())    .isEqualTo(TaskPriority.HIGH);
    }

    @Test void taskBean_toDto_idAndVersionPropagated()
    {
        // simulate a bean loaded from DB (via DTO → Bean → roundtrip)
        TaskGroupDto  gDto   = persistedGroupDto(1L, (short) 0, "G");
        TaskGroupBean gBean  = new TaskGroupBean(gDto);
        TaskDto       tDto   = persistedTaskDto(5L, (short) 2, "T", gDto);
        TaskBean      tBean  = new TaskBean(gBean, tDto);

        TaskDto out = Mappings.toDto(tBean);

        assertThat(out.id())     .isEqualTo(5L);
        assertThat(out.version()).isEqualTo((short) 2);
    }

    @Test void taskBean_toDto_sameGroupInstanceReused()
    {
        TaskDto dtoA = Mappings.toDto(taskA);
        TaskDto dtoB = Mappings.toDto(taskB);

        // different calls → different ctx → different group DTO instances
        assertThat(dtoA.taskGroup().name()).isEqualTo("Sprint 1");
        assertThat(dtoB.taskGroup().name()).isEqualTo("Sprint 1");
    }

    @Test void taskList_toDto_sharedGroupInstancePerCall()
    {
        // toDto(TaskBean) individually → two separate ctx, two group DTOs
        TaskDto dtoA = Mappings.toDto(taskA);
        TaskDto dtoB = Mappings.toDto(taskB);
        assertThat(dtoA.taskGroup()).isNotSameAs(dtoB.taskGroup());
    }

    // ── predecessor/successor mapping ──────────────────────────────────────

    @Test void taskBean_toDto_predecessorMapped()
    {
        taskB.addPredecessor(taskA);

        TaskDto dtoB = Mappings.toDto(taskB);

        assertThat(dtoB.predecessors()).hasValueSatisfying(preds ->
            assertThat(preds).anyMatch(p -> "Task A".equals(p.name())));
    }

    @Test void taskBean_toDto_successorMapped()
    {
        taskA.addSuccessor(taskB);

        TaskDto dtoA = Mappings.toDto(taskA);

        assertThat(dtoA.successors()).hasValueSatisfying(succs ->
            assertThat(succs).anyMatch(s -> "Task B".equals(s.name())));
    }

    // ── TaskDto → TaskBean ─────────────────────────────────────────────────

    @Test void taskDto_toBean_scalarFields()
    {
        LocalDate start = LocalDate.of(2026, 2, 1);
        TaskGroupDto gDto = new TaskGroupDto("G");
        TaskDto      tDto = persistedTaskDto(3L, (short) 1, "T", gDto)
            .description("hello")
            .scheduledStart(start)
            .status(TaskStatus.ON_HOLD)
            .priority(TaskPriority.IMMEDIATE);

        TaskBean bean = Mappings.toBean(tDto);

        assertThat(bean.name())        .isEqualTo("T");
        assertThat(bean.id())          .isEqualTo(3L);
        assertThat(bean.version())     .isEqualTo((short) 1);
        assertThat(bean.description()) .hasValue("hello");
        assertThat(bean.scheduledStart()).hasValue(start);
        assertThat(bean.status())      .isEqualTo(TaskStatus.ON_HOLD);
        assertThat(bean.priority())    .isEqualTo(TaskPriority.IMMEDIATE);
    }

    @Test void taskDto_toBean_groupPreserved()
    {
        TaskGroupDto gDto = new TaskGroupDto("G2");
        TaskDto      tDto = new TaskDto("T", gDto);
        TaskBean     bean = Mappings.toBean(tDto);

        assertThat(bean.taskGroup().name()).isEqualTo("G2");
    }

    @Test void taskDto_toBean_contextDeduplicatesGroup()
    {
        TaskGroupDto gDto = new TaskGroupDto("G");
        TaskDto      t1   = new TaskDto("T1", gDto);
        TaskDto      t2   = new TaskDto("T2", gDto);

        List<TaskBean> beans = Mappings.toBean(List.of(t1, t2));

        // both tasks must belong to the same TaskGroupBean instance
        assertThat(beans.get(0).taskGroup()).isSameAs(beans.get(1).taskGroup());
    }

    // ── subTask hierarchy ──────────────────────────────────────────────────

    @Test void taskDto_toBean_parentTaskMapped()
    {
        TaskGroupDto gDto   = new TaskGroupDto("G");
        TaskDto      parent = new TaskDto("Parent", gDto);
        TaskDto      child  = new TaskDto("Child",  gDto);
        parent.addSubTask(child);

        TaskBean parentBean = Mappings.toBean(parent);

        assertThat(parentBean.subTasks()).hasValueSatisfying(ts ->
            assertThat(ts).anyMatch(t -> "Child".equals(t.name())));
        TaskBean childBean = parentBean.subTasks().get().iterator().next();
        assertThat(childBean.parentTask()).hasValue(parentBean);
    }

    private TaskGroupDto persistedGroupDto(long id, short version, String name)
    {
        return new TaskGroupDto(new TestPersistentTaskGroup(id, version, name));
    }

    private TaskDto persistedTaskDto(long id, short version, String name, TaskGroupDto group)
    {
        return new TaskDto(group, new TestPersistentTask(id, version, name, group));
    }

    private record TestPersistentTaskGroup(Long id, Short version, String name)
            implements PersistentTaskGroup<TestPersistentTask>
    {
        @Override public Optional<Set<TestPersistentTask>> tasks() { return Optional.empty(); }
        @Override public void addTask(TestPersistentTask task) { throw new UnsupportedOperationException(); }
        @Override public void removeTask(TestPersistentTask task) { throw new UnsupportedOperationException(); }
        @Override public PersistentTaskGroup<TestPersistentTask> name(String name) { throw new UnsupportedOperationException(); }
    }

    private record TestPersistentTask(Long id, Short version, String name, TaskGroupDto taskGroup)
            implements PersistentTask<TaskGroupDto, TestPersistentTask>
    {
        @Override public Optional<TestPersistentTask> parentTask() { return Optional.empty(); }
        @Override public Optional<Set<TestPersistentTask>> subTasks() { return Optional.empty(); }
        @Override public Optional<Set<TestPersistentTask>> predecessors() { return Optional.empty(); }
        @Override public Optional<Set<TestPersistentTask>> successors() { return Optional.empty(); }
        @Override public Optional<String> description() { return Optional.empty(); }
        @Override public Optional<Double> workEstimateInitial() { return Optional.empty(); }
        @Override public Optional<Double> workEstimateCurrent() { return Optional.empty(); }
        @Override public Optional<Double> workActual() { return Optional.empty(); }
        @Override public Optional<LocalDate> scheduledStart() { return Optional.empty(); }
        @Override public Optional<LocalDate> scheduledFinish() { return Optional.empty(); }
        @Override public TaskStatus status() { return TaskStatus.NEW; }
        @Override public TaskPriority priority() { return TaskPriority.NORMAL; }
        @Override public TestPersistentTask name(String name) { throw new UnsupportedOperationException(); }
        @Override public TestPersistentTask parentTask(TestPersistentTask parentTask) { throw new UnsupportedOperationException(); }
        @Override public void addSubTask(TestPersistentTask task) { throw new UnsupportedOperationException(); }
        @Override public void removeSubTask(TestPersistentTask task) { throw new UnsupportedOperationException(); }
        @Override public void addPredecessor(TestPersistentTask task) { throw new UnsupportedOperationException(); }
        @Override public void removePredecessor(TestPersistentTask task) { throw new UnsupportedOperationException(); }
        @Override public void addSuccessor(TestPersistentTask task) { throw new UnsupportedOperationException(); }
        @Override public void removeSuccessor(TestPersistentTask task) { throw new UnsupportedOperationException(); }
        @Override public TaskGroupDto taskGroup() { return taskGroup; }
        @Override public TestPersistentTask taskGroup(TaskGroupDto taskGroup) { throw new UnsupportedOperationException(); }
        @Override public TestPersistentTask description(String description) { throw new UnsupportedOperationException(); }
        @Override public TestPersistentTask workEstimateInitial(Double workEstimateInitial) { throw new UnsupportedOperationException(); }
        @Override public TestPersistentTask workEstimateCurrent(Double workEstimateCurrent) { throw new UnsupportedOperationException(); }
        @Override public TestPersistentTask workActual(Double workActual) { throw new UnsupportedOperationException(); }
        @Override public TestPersistentTask scheduledStart(LocalDate scheduledStart) { throw new UnsupportedOperationException(); }
        @Override public TestPersistentTask scheduledFinish(LocalDate scheduledFinish) { throw new UnsupportedOperationException(); }
        @Override public TestPersistentTask status(TaskStatus status) { throw new UnsupportedOperationException(); }
        @Override public TestPersistentTask priority(TaskPriority priority) { throw new UnsupportedOperationException(); }
    }
}
