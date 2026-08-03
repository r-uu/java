package de.ruu.app.pragma.fx.task.inspector;

import de.ruu.app.pragma.bean.TaskBean;
import de.ruu.app.pragma.fx.task.edit.TaskEditorService;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.Consumer;

public final class TaskInspectorSupport
{
  @FunctionalInterface
  public interface SaveAction
  {
    TaskBean save(TaskBean task) throws Exception;
  }

  private static final String INSPECTOR_HIDE_ICON = "fas-angle-right";
  private static final String INSPECTOR_SHOW_ICON = "fas-angle-left";
  private static final String INSPECTOR_SAVE_DIRTY_STYLE =
      "-fx-background-color: #cc3333; -fx-text-fill: white;";

  private final BorderPane mainPane;
  private final VBox inspectorContainer;
  private final Button toggleButton;
  private final Button saveButton;
  private final Parent editorRoot;
  private final TaskEditorService editorService;
  private final SaveAction saveAction;
  private final Consumer<TaskBean> onSaved;
  private final Consumer<Exception> onSaveError;

  private TaskBean currentTask;
  private boolean inspectorVisible = true;

  public TaskInspectorSupport(
      BorderPane mainPane,
      VBox inspectorContainer,
      Button toggleButton,
      Button saveButton,
      Parent editorRoot,
      TaskEditorService editorService,
      SaveAction saveAction,
      Consumer<TaskBean> onSaved,
      Consumer<Exception> onSaveError)
  {
    this.mainPane = mainPane;
    this.inspectorContainer = inspectorContainer;
    this.toggleButton = toggleButton;
    this.saveButton = saveButton;
    this.editorRoot = editorRoot;
    this.editorService = editorService;
    this.saveAction = saveAction;
    this.onSaved = onSaved;
    this.onSaveError = onSaveError;
  }

  public void initialize()
  {
    if (!inspectorContainer.getChildren().contains(editorRoot))
      inspectorContainer.getChildren().add(editorRoot);
    editorService.setEditable(false);
    editorService.onTaskUpdated(this::onEditorTaskUpdated);
    editorService.dirtyProperty().addListener((obs, old, dirty) -> updateSaveButtonState());
    toggleButton.setOnAction(e -> toggleInspector());
    saveButton.setOnAction(e -> saveCurrentTask());
    updateInspectorVisibility();
    clearTask();
  }

  public void showTask(TaskBean task)
  {
    currentTask = task;
    editorService.task(task);
    editorService.setEditable(true);
    updateSaveButtonState();
  }

  public void clearTask()
  {
    currentTask = null;
    editorService.clear();
    editorService.setEditable(false);
    updateSaveButtonState();
  }

  public void clearDirty()
  {
    editorService.clearDirty();
    updateSaveButtonState();
  }

  public boolean dirty()
  {
    return editorService.dirtyProperty().get();
  }

  private void saveCurrentTask()
  {
    if (currentTask == null || currentTask.id() == null || !dirty()) return;

    try
    {
      editorService.applyTo(currentTask);
      TaskBean updated = saveAction.save(currentTask);
      currentTask = updated;
      editorService.task(updated);
      editorService.clearDirty();
      onSaved.accept(updated);
    }
    catch (Exception e)
    {
      onSaveError.accept(e);
    }
    updateSaveButtonState();
  }

  private void onEditorTaskUpdated(TaskBean updated)
  {
    if (updated == null) return;
    currentTask = updated;
    onSaved.accept(updated);
    updateSaveButtonState();
  }

  private void toggleInspector()
  {
    inspectorVisible = !inspectorVisible;
    updateInspectorVisibility();
  }

  private void updateInspectorVisibility()
  {
    if (inspectorVisible)
    {
      if (mainPane.getRight() == null) mainPane.setRight(inspectorContainer);
      toggleButton.setText(null);
      toggleButton.setGraphic(icon(INSPECTOR_HIDE_ICON));
      toggleButton.setTooltip(new Tooltip("Hide inspector"));
      return;
    }
    if (mainPane.getRight() != null) mainPane.setRight(null);
    toggleButton.setText(null);
    toggleButton.setGraphic(icon(INSPECTOR_SHOW_ICON));
    toggleButton.setTooltip(new Tooltip("Show inspector"));
  }

  private void updateSaveButtonState()
  {
    boolean saveEnabled = currentTask != null && currentTask.id() != null && dirty();
    saveButton.setDisable(!saveEnabled);
    saveButton.setStyle(saveEnabled ? INSPECTOR_SAVE_DIRTY_STYLE : "");
  }

  private static FontIcon icon(String iconLiteral)
  {
    FontIcon icon = new FontIcon(iconLiteral);
    icon.setIconSize(11);
    return icon;
  }
}
