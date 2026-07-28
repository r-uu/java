package de.ruu.app.pragma.fx.task.inspector;

import de.ruu.app.pragma.bean.TaskBean;
import de.ruu.app.pragma.bean.TaskGroupBean;
import de.ruu.app.pragma.fx.task.edit.TaskEditorService;
import de.ruu.app.pragma.fx.test.FxTestHarness;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class TaskInspectorSupportTest
{
  @BeforeAll
  static void startFx()
  {
    FxTestHarness.startFx();
  }

  @Test
  void saveButtonTurnsRedWhenDirty()
  {
    FakeEditorService editor = new FakeEditorService();
    BorderPane pane = new BorderPane();
    VBox container = new VBox();
    Button toggle = new Button();
    Button save = new Button();
    TaskBean task = persistedTask(1L, "A");

    FxTestHarness.runOnFxThread(() -> {
      TaskInspectorSupport support = new TaskInspectorSupport(
          pane, container, toggle, save, new Pane(), editor,
          in -> in, updated -> {}, error -> { throw new AssertionError(error); });
      support.initialize();
      support.showTask(task);
      assertThat(save.isDisable()).isTrue();
      editor.dirtyProperty().set(true);
      assertThat(save.isDisable()).isFalse();
      assertThat(save.getStyle()).contains("#cc3333");
    });
  }

  @Test
  void saveRunsCallbackAndClearsDirty()
  {
    FakeEditorService editor = new FakeEditorService();
    BorderPane pane = new BorderPane();
    VBox container = new VBox();
    Button toggle = new Button();
    Button save = new Button();
    TaskBean task = persistedTask(2L, "B");
    AtomicBoolean saveCalled = new AtomicBoolean(false);
    AtomicBoolean onSavedCalled = new AtomicBoolean(false);

    FxTestHarness.runOnFxThread(() -> {
      TaskInspectorSupport support = new TaskInspectorSupport(
          pane, container, toggle, save, new Pane(), editor,
          in -> {
            saveCalled.set(true);
            in.name("B2");
            return in;
          },
          updated -> onSavedCalled.set(true),
          error -> { throw new AssertionError(error); });
      support.initialize();
      support.showTask(task);
      editor.dirtyProperty().set(true);
      save.fire();
      assertThat(saveCalled.get()).isTrue();
      assertThat(onSavedCalled.get()).isTrue();
      assertThat(editor.dirtyProperty().get()).isFalse();
      assertThat(task.name()).isEqualTo("B2");
    });
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

  private static final class FakeEditorService implements TaskEditorService
  {
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);
    private boolean updating;
    private TaskBean task;
    private Consumer<TaskBean> onTaskUpdated = ignored -> {};

    @Override public Optional<TaskBean> task() { return Optional.ofNullable(task); }
    @Override public void task(TaskBean task) { this.task = task; }
    @Override public void clear() { task = null; }
    @Override public void applyTo(TaskBean task) { this.task = task; }
    @Override public void setEditable(boolean editable) { }
    @Override public void onTaskUpdated(Consumer<TaskBean> listener) { this.onTaskUpdated = listener; }
    @Override public BooleanProperty dirtyProperty() { return dirty; }
    @Override public void clearDirty() { dirty.set(false); }
    @Override public boolean isUpdating() { return updating; }
    @Override public void beginUpdating() { updating = true; }
    @Override public void endUpdating() { updating = false; }
  }
}
