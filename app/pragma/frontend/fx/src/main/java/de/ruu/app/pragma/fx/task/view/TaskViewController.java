package de.ruu.app.pragma.fx.task.view;

import de.ruu.app.pragma.bean.TaskBean;
import de.ruu.lib.fx.FXUtil;
import de.ruu.lib.fx.comp.FXCController.DefaultFXCController;
import jakarta.enterprise.context.Dependent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.util.Optional;

@Dependent
class TaskViewController extends DefaultFXCController<TaskView, TaskViewService> implements TaskViewService
{
  @FXML private VBox root;
  @FXML private GridPane grdPnCore;
  @FXML private GridPane grdPnEstimates;
  @FXML private GridPane grdPnWork;
  @FXML private HBox hBxWork;

  @FXML private TextField tfId;
  @FXML private TextField tfName;
  @FXML private DatePicker dtPckrStart;
  @FXML private DatePicker dtPckrEnd;
  @FXML private TextArea taDescription;
  @FXML private CheckBox chkBxClosed;

  @FXML private TextField tfWorkEstimateInitial;
  @FXML private TextField tfWorkEstimateCurrent;
  @FXML private TextField tfWorkActual;
  @FXML private TextField tfWorkRemaining;
  @FXML private TextField tfWorkProgress;

  private final BooleanProperty dirty = new SimpleBooleanProperty(false);
  private boolean updating = false;
  private TaskBean task;

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

    tfName.textProperty().addListener((obs, o, n) -> { if (!updating) dirty.set(true); });
    dtPckrStart.valueProperty().addListener((obs, o, n) -> { if (!updating) dirty.set(true); });
    dtPckrEnd.valueProperty().addListener((obs, o, n) -> { if (!updating) dirty.set(true); });
    taDescription.textProperty().addListener((obs, o, n) -> { if (!updating) dirty.set(true); });
    chkBxClosed.selectedProperty().addListener((obs, o, n) -> { if (!updating) dirty.set(true); });

    tfWorkEstimateInitial.textProperty().addListener((obs, o, n) -> {
      if (!updating) dirty.set(true);
      refreshDerivedWorkFields();
    });
    tfWorkEstimateCurrent.textProperty().addListener((obs, o, n) -> {
      if (!updating) dirty.set(true);
      refreshDerivedWorkFields();
    });
    tfWorkActual.textProperty().addListener((obs, o, n) -> {
      if (!updating) dirty.set(true);
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
    updating = true;
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
      chkBxClosed.setSelected(task.closed());
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
    task = null;
    updating = true;
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
      chkBxClosed.setSelected(false);
    }
    finally
    {
      updating = false;
      dirty.set(false);
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
    task.closed(chkBxClosed.isSelected());
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
    chkBxClosed.setDisable(!editable);
  }

  @Override public BooleanProperty dirtyProperty() { return dirty; }
  @Override public void clearDirty() { dirty.set(false); }

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
      throw new IllegalArgumentException("Ungültiger Wert für " + label + ": " + value, e);
    }
  }

  private static String formatDouble(Double value)
  {
    return value == null ? "" : BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
  }
}
