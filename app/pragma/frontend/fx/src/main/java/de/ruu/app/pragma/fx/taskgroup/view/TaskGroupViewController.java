package de.ruu.app.pragma.fx.taskgroup.view;

import de.ruu.app.pragma.bean.TaskGroupBean;
import de.ruu.lib.fx.comp.FXCController.DefaultFXCController;
import jakarta.enterprise.context.Dependent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

@Dependent
class TaskGroupViewController extends DefaultFXCController<TaskGroupView, TaskGroupViewService> implements TaskGroupViewService
{
  @FXML private GridPane grdPn;
  @FXML private TextField tfId;
  @FXML private TextField tfName;

  private final BooleanProperty dirty = new SimpleBooleanProperty(false);
  private boolean updating = false;
  private TaskGroupBean taskGroup;

  @Override
  @FXML
  protected void initialize()
  {
    tfId.setEditable(false);
    tfId.setStyle("-fx-background-color: transparent;");
    tfName.textProperty().addListener((obs, o, n) -> { if (!updating) dirty.set(true); });
  }

  @Override public @NonNull Optional<TaskGroupBean> taskGroup() { return Optional.ofNullable(taskGroup); }

  @Override
  public void taskGroup(TaskGroupBean taskGroup)
  {
    this.taskGroup = taskGroup;
    updating = true;
    try
    {
      tfId.setText(taskGroup.id() == null ? "" : taskGroup.id().toString());
      tfName.setText(taskGroup.name());
    }
    finally
    {
      updating = false;
      dirty.set(false);
    }
  }

  @Override
  public void clear()
  {
    taskGroup = null;
    updating = true;
    try
    {
      tfId.clear();
      tfName.clear();
    }
    finally
    {
      updating = false;
      dirty.set(false);
    }
  }

  @Override
  public void applyTo(TaskGroupBean taskGroup)
  {
    String raw = tfName.getText() == null ? "" : tfName.getText().trim();
    if (raw.isEmpty()) return;
    taskGroup.name(raw);
  }

  @Override public void setEditable(boolean editable) { tfName.setEditable(editable); }
  @Override public BooleanProperty dirtyProperty() { return dirty; }
  @Override public void clearDirty() { dirty.set(false); }
}
