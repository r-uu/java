package de.ruu.app.pragma.fx.task.edit;

import de.ruu.app.pragma.bean.TaskBean;
import de.ruu.app.pragma.fx.task.view.TaskView;
import de.ruu.lib.fx.comp.FXCController.DefaultFXCController;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

@Dependent
class TaskEditorController extends DefaultFXCController<TaskEditor, TaskEditorService> implements TaskEditorService
{
  @FXML private AnchorPane root;
  @FXML private VBox vBxContainerEditor;

  @Inject private TaskView taskView;

  private final BooleanProperty dirty = new SimpleBooleanProperty(false);
  private boolean updating = false;

  @Override
  @FXML
  protected void initialize()
  {
    // The editor composes the reusable TaskView and turns it into an editable form.
    vBxContainerEditor.getChildren().add(taskView.localRoot());
    taskView.service().setEditable(true);
  }

  @Override public @NonNull Optional<TaskBean> task() { return taskView.service().task(); }
  @Override public void task(TaskBean task) { taskView.service().task(task); }
  @Override public void clear() { taskView.service().clear(); }
  @Override public void applyTo(TaskBean task) { taskView.service().applyTo(task); }
  @Override public void setEditable(boolean editable) { taskView.service().setEditable(editable); }
  @Override public BooleanProperty dirtyProperty() { return dirty; }
  @Override public void clearDirty() { dirty.set(false); }
  @Override public boolean isUpdating() { return updating; }
  @Override public void beginUpdating() { updating = true; }
  @Override public void endUpdating() { updating = false; }
}
