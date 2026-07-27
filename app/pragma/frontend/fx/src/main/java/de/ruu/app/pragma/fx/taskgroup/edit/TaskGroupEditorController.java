package de.ruu.app.pragma.fx.taskgroup.edit;

import de.ruu.app.pragma.bean.TaskGroupBean;
import de.ruu.app.pragma.fx.taskgroup.view.TaskGroupView;
import de.ruu.lib.fx.comp.FXCController.DefaultFXCController;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

@Dependent
class TaskGroupEditorController extends DefaultFXCController<TaskGroupEditor, TaskGroupEditorService>
    implements TaskGroupEditorService
{
  @FXML private AnchorPane root;
  @FXML private VBox vBxContainerEditor;

  @Inject private TaskGroupView taskGroupView;

  @Override
  @FXML
  protected void initialize()
  {
    // Same composition pattern as TaskEditor: reuse the view and expose it as an editable form.
    vBxContainerEditor.getChildren().add(taskGroupView.localRoot());
    taskGroupView.service().setEditable(true);
  }

  @Override public @NonNull Optional<TaskGroupBean> taskGroup() { return taskGroupView.service().taskGroup(); }
  @Override public void taskGroup(TaskGroupBean taskGroup) { taskGroupView.service().taskGroup(taskGroup); }
  @Override public void clear() { taskGroupView.service().clear(); }
  @Override public void applyTo(TaskGroupBean taskGroup) { taskGroupView.service().applyTo(taskGroup); }
  @Override public void setEditable(boolean editable) { taskGroupView.service().setEditable(editable); }
  @Override public BooleanProperty dirtyProperty() { return taskGroupView.service().dirtyProperty(); }
  @Override public void clearDirty() { taskGroupView.service().clearDirty(); }
}
