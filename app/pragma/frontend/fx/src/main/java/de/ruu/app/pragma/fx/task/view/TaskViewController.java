package de.ruu.app.pragma.fx.task.view;

import de.ruu.app.pragma.bean.TaskBean;
import de.ruu.app.pragma.core.TaskPriority;
import de.ruu.app.pragma.core.TaskStatus;
import de.ruu.app.pragma.fx.task.edit.TaskEditorService;
import de.ruu.lib.fx.FXUtil;
import de.ruu.lib.fx.comp.FXCController.DefaultFXCController;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.util.Optional;

@Dependent
class TaskViewController extends DefaultFXCController<TaskView, TaskViewService> implements TaskViewService
{
  @FXML private VBox root;
  @FXML private GridPane grdPnEstimates;
  @FXML private GridPane grdPnWork;
  @FXML private GridPane grdPnPlanning;

  @FXML private TextField tfId;
  @FXML private TextField tfName;
  @FXML private DatePicker dtPckrStart;
  @FXML private DatePicker dtPckrEnd;
  @FXML private TextArea taDescription;
  @FXML private ComboBox<TaskStatus> cbxStatus;
  @FXML private ComboBox<TaskPriority> cbxPriority;

  @FXML private TextField tfWorkEstimateInitial;
  @FXML private TextField tfWorkEstimateCurrent;
  @FXML private TextField tfWorkActual;
  @FXML private TextField tfWorkRemaining;
  @FXML private TextField tfWorkProgress;

  private TaskBean task;

  @Inject private Instance<TaskEditorService> taskEditorService;

  @Override
  @FXML
  protected void initialize()
  {
    tfId.setEditable(false);
    tfId.setStyle("-fx-background-color: transparent;");
    tfWorkRemaining.setEditable(false);
    tfWorkProgress.setEditable(false);

    taDescription.setWrapText(true);

    FXUtil.wrapInTitledBorder("estimates", grdPnEstimates);
    FXUtil.wrapInTitledBorder("work", grdPnWork);
    FXUtil.wrapInTitledBorder("planning", grdPnPlanning);

    tfName.textProperty().addListener((obs, o, n) -> markDirty());
    dtPckrStart.valueProperty().addListener((obs, o, n) -> markDirty());
    dtPckrEnd.valueProperty().addListener((obs, o, n) -> markDirty());
    taDescription.textProperty().addListener((obs, o, n) -> markDirty());
    cbxStatus.valueProperty().addListener((obs, o, n) -> markDirty());
    cbxPriority.valueProperty().addListener((obs, o, n) -> markDirty());
    // The combo box shows TaskStatus.toString(), so the user sees the same labels as the API wire format.
    cbxStatus.setItems(FXCollections.observableArrayList(TaskStatus.values()));
    // Same pattern for priority: enum values are shown with the same wire labels as persisted/sent by the API.
    cbxPriority.setItems(FXCollections.observableArrayList(TaskPriority.values()));

    tfWorkEstimateInitial.textProperty().addListener((obs, o, n) -> {
      markDirty();
      refreshDerivedWorkFields();
    });
    tfWorkEstimateCurrent.textProperty().addListener((obs, o, n) -> {
      markDirty();
      refreshDerivedWorkFields();
    });
    tfWorkActual.textProperty().addListener((obs, o, n) -> {
      markDirty();
      refreshDerivedWorkFields();
    });
  }

  @Override
  public @NonNull Optional<TaskBean> task()
  {
    return Optional.ofNullable(task);
  }

  @Override
  public void task(TaskBean task)
  {
    this.task = task;
    taskEditorService.get().beginUpdating();
    try
    {
      tfId.setText(task.id() == null ? "" : task.id().toString());
      tfName.setText(task.name());
      dtPckrStart.setValue(task.scheduledStart().orElse(null));
      dtPckrEnd.setValue(task.scheduledFinish().orElse(null));
      taDescription.setText(task.description().orElse(""));
      tfWorkEstimateInitial.setText(formatDouble(task.workEstimateInitial().orElse(null)));
      tfWorkEstimateCurrent.setText(formatDouble(task.workEstimateCurrent().orElse(null)));
      tfWorkActual.setText(formatDouble(task.workActual().orElse(null)));
      tfWorkRemaining.setText(formatDouble(task.workRemaining().orElse(null)));
      tfWorkProgress.setText(formatDouble(task.workProgress().orElse(null)));
      cbxStatus.setValue(task.status());
      cbxPriority.setValue(task.priority());
    }
    finally
    {
      taskEditorService.get().endUpdating();
      taskEditorService.get().clearDirty();
    }
  }

  @Override
  public void clear()
  {
    task = null;
    taskEditorService.get().beginUpdating();
    try
    {
      tfId.clear();
      tfName.clear();
      dtPckrStart.setValue(null);
      dtPckrEnd.setValue(null);
      taDescription.clear();
      tfWorkEstimateInitial.clear();
      tfWorkEstimateCurrent.clear();
      tfWorkActual.clear();
      tfWorkRemaining.clear();
      tfWorkProgress.clear();
      cbxStatus.setValue(null);
      cbxPriority.setValue(null);
    }
    finally
    {
      taskEditorService.get().endUpdating();
      taskEditorService.get().clearDirty();
    }
  }

  @Override
  public void applyTo(TaskBean task)
  {
    String name = tfName.getText() == null ? "" : tfName.getText().trim();
    if (!name.isEmpty()) task.name(name);
    task.scheduledStart(dtPckrStart.getValue());
    task.scheduledFinish(dtPckrEnd.getValue());
    task.description(taDescription.getText().isBlank() ? null : taDescription.getText());
    task.workEstimateInitial(parseStrictHours(tfWorkEstimateInitial.getText(), "work estimate initial"));
    task.workEstimateCurrent(parseStrictHours(tfWorkEstimateCurrent.getText(), "work estimate current"));
    task.workActual(parseStrictHours(tfWorkActual.getText(), "work actual"));
    task.status(cbxStatus.getValue() == null ? TaskStatus.NEW : cbxStatus.getValue());
    task.priority(cbxPriority.getValue() == null ? TaskPriority.NORMAL : cbxPriority.getValue());
  }

  @Override
  public void setEditable(boolean editable)
  {
    tfName.setEditable(editable);
    dtPckrStart.setDisable(!editable);
    dtPckrEnd.setDisable(!editable);
    taDescription.setEditable(editable);
    tfWorkEstimateInitial.setEditable(editable);
    tfWorkEstimateCurrent.setEditable(editable);
    tfWorkActual.setEditable(editable);
    cbxStatus.setDisable(!editable);
    cbxPriority.setDisable(!editable);
  }

  @Override public BooleanProperty dirtyProperty() { return editor().dirtyProperty(); }
  @Override public void clearDirty() { editor().clearDirty(); }

  private void refreshDerivedWorkFields()
  {
    Optional<Double> estimateCurrent = parseLenientHours(tfWorkEstimateCurrent.getText());
    Optional<Double> workActual = parseLenientHours(tfWorkActual.getText());
    Optional<Double> workRemaining =
        estimateCurrent.flatMap(estimate -> workActual.map(actual -> estimate - actual));
    Optional<Double> workProgress =
        workActual.flatMap(actual -> workRemaining.map(remaining -> {
          double denominator = actual + remaining;
          return denominator == 0 ? 0 : (actual / denominator) * 100;
        }));

    tfWorkRemaining.setText(formatDouble(workRemaining.orElse(null)));
    tfWorkProgress.setText(formatDouble(workProgress.orElse(null)));
  }

  private void markDirty()
  {
    if (!editor().isUpdating()) editor().dirtyProperty().set(true);
  }

  private TaskEditorService editor()
  {
    return taskEditorService.get();
  }

  private static Optional<Double> parseLenientHours(String raw)
  {
    String value = raw == null ? "" : raw.trim();
    if (value.isEmpty()) return Optional.empty();
    try
    {
      return Optional.of(Double.parseDouble(value.replace(',', '.')));
    }
    catch (NumberFormatException e)
    {
      return Optional.empty();
    }
  }

  private static Double parseStrictHours(String raw, String label)
  {
    String value = raw == null ? "" : raw.trim();
    if (value.isEmpty()) return null;
    try
    {
      return Double.parseDouble(value.replace(',', '.'));
    }
    catch (NumberFormatException e)
    {
      throw new IllegalArgumentException("Invalid value for " + label + ": " + value, e);
    }
  }

  private static String formatDouble(Double value)
  {
    return value == null ? "" : BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
  }
}
