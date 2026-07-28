package de.ruu.app.pragma.fx.task.view;

import de.ruu.app.pragma.bean.TaskBean;
import de.ruu.app.pragma.client.TaskClient;
import de.ruu.app.pragma.core.TaskPriority;
import de.ruu.app.pragma.core.TaskStatus;
import de.ruu.app.pragma.fx.task.TaskUiSupport;
import de.ruu.app.pragma.fx.task.edit.TaskEditorService;
import de.ruu.lib.fx.FXUtil;
import de.ruu.lib.fx.comp.FXCController.DefaultFXCController;
import de.ruu.lib.fx.control.autocomplete.textfield.TextFieldAutoCompleteClearableWithArrowButton;
import de.ruu.lib.fx.control.autocomplete.textfield.TextFieldAutoCompleteClearableWithArrowButtonBuilder;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

@Dependent
class TaskViewController extends DefaultFXCController<TaskView, TaskViewService> implements TaskViewService
{
  private static final Logger log = LogManager.getLogger(TaskViewController.class);

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
  @FXML private TextField tfPredecessors;
  @FXML private TextField tfSuccessors;
  @FXML private Button btnAddPredecessor;
  @FXML private Button btnRemovePredecessor;
  @FXML private Button btnAddSuccessor;
  @FXML private Button btnRemoveSuccessor;

  private TaskBean task;
  private TaskEditorService editorService;
  private Consumer<TaskBean> taskUpdatedListener = ignored -> {};
  private boolean editable = true;

  @Inject private TaskClient taskClient;

  @Override
  @FXML
  protected void initialize()
  {
    tfId.setEditable(false);
    tfId.setStyle("-fx-background-color: transparent;");
    tfWorkRemaining.setEditable(false);
    tfWorkProgress.setEditable(false);
    tfPredecessors.setEditable(false);
    tfPredecessors.setStyle("-fx-background-color: transparent;");
    tfSuccessors.setEditable(false);
    tfSuccessors.setStyle("-fx-background-color: transparent;");

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

    btnAddPredecessor.setOnAction(e -> addPredecessor());
    btnRemovePredecessor.setOnAction(e -> removePredecessor());
    btnAddSuccessor.setOnAction(e -> addSuccessor());
    btnRemoveSuccessor.setOnAction(e -> removeSuccessor());
    updateRelationButtons();
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
    editor().beginUpdating();
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
      refreshRelationFields();
      updateRelationButtons();
    }
    finally
    {
      editor().endUpdating();
      editor().clearDirty();
    }
  }

  @Override
  public void clear()
  {
    task = null;
    editor().beginUpdating();
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
      tfPredecessors.clear();
      tfSuccessors.clear();
      updateRelationButtons();
    }
    finally
    {
      editor().endUpdating();
      editor().clearDirty();
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
    this.editable = editable;
    tfName.setEditable(editable);
    dtPckrStart.setDisable(!editable);
    dtPckrEnd.setDisable(!editable);
    taDescription.setEditable(editable);
    tfWorkEstimateInitial.setEditable(editable);
    tfWorkEstimateCurrent.setEditable(editable);
    tfWorkActual.setEditable(editable);
    cbxStatus.setDisable(!editable);
    cbxPriority.setDisable(!editable);
    updateRelationButtons();
  }

  @Override
  public void bindEditorService(TaskEditorService editorService)
  {
    this.editorService = editorService;
  }

  @Override
  public void onTaskUpdated(Consumer<TaskBean> listener)
  {
    this.taskUpdatedListener = listener == null ? ignored -> {} : listener;
  }

  @Override public BooleanProperty dirtyProperty() { return editor().dirtyProperty(); }
  @Override public void clearDirty() { editor().clearDirty(); }

  private void addPredecessor()
  {
    if (task == null || task.id() == null) return;
    final long taskId = task.id();
    Set<Long> excluded = new HashSet<>();
    excluded.add(taskId);
    task.predecessors().ifPresent(preds -> preds.stream().map(TaskBean::id).forEach(excluded::add));
    pickTask("Add predecessor", excluded).ifPresent(pred -> {
      if (pred.id() == null) return;
      if (excluded.contains(pred.id()))
      {
        TaskUiSupport.showInfo("Add predecessor", "Selected task is already a predecessor.");
        return;
      }
      Map<Long, Optional<TaskBean>> cache = new HashMap<>();
      Function<Long, Optional<TaskBean>> provider = id -> cache.computeIfAbsent(id, key -> taskClient.findByIdWithRelated(key));
      if (TaskRelationRules.wouldCreateCycle(pred.id(), taskId, provider))
      {
        TaskUiSupport.showWarning("Add predecessor", "Relation would create a dependency cycle.");
        return;
      }
      try
      {
        taskClient.addPredecessor(task, pred);
        refreshTaskWithRelations();
      }
      catch (Exception e)
      {
        log.error("failed to add predecessor", e);
        TaskUiSupport.showError("Add predecessor", e);
      }
    });
  }

  private void removePredecessor()
  {
    if (task == null || task.id() == null) return;
    List<TaskBean> predecessors = task.predecessors().map(Set::stream).orElseGet(java.util.stream.Stream::empty)
        .filter(t -> t.id() != null)
        .sorted(Comparator.comparing(TaskBean::name, String.CASE_INSENSITIVE_ORDER))
        .toList();
    if (predecessors.isEmpty())
    {
      TaskUiSupport.showInfo("Remove predecessor", "Task has no predecessors.");
      return;
    }
    pickExisting("Remove predecessor", predecessors).ifPresent(pred -> {
      try
      {
        taskClient.removePredecessor(task, pred);
        refreshTaskWithRelations();
      }
      catch (Exception e)
      {
        log.error("failed to remove predecessor", e);
        TaskUiSupport.showError("Remove predecessor", e);
      }
    });
  }

  private void addSuccessor()
  {
    if (task == null || task.id() == null) return;
    final long taskId = task.id();
    Set<Long> excluded = new HashSet<>();
    excluded.add(taskId);
    task.successors().ifPresent(succs -> succs.stream().map(TaskBean::id).forEach(excluded::add));
    pickTask("Add successor", excluded).ifPresent(succ -> {
      if (succ.id() == null) return;
      if (excluded.contains(succ.id()))
      {
        TaskUiSupport.showInfo("Add successor", "Selected task is already a successor.");
        return;
      }
      Map<Long, Optional<TaskBean>> cache = new HashMap<>();
      Function<Long, Optional<TaskBean>> provider = id -> cache.computeIfAbsent(id, key -> taskClient.findByIdWithRelated(key));
      if (TaskRelationRules.wouldCreateCycle(taskId, succ.id(), provider))
      {
        TaskUiSupport.showWarning("Add successor", "Relation would create a dependency cycle.");
        return;
      }
      try
      {
        taskClient.addPredecessor(succ, task);
        refreshTaskWithRelations();
      }
      catch (Exception e)
      {
        log.error("failed to add successor", e);
        TaskUiSupport.showError("Add successor", e);
      }
    });
  }

  private void removeSuccessor()
  {
    if (task == null || task.id() == null) return;
    List<TaskBean> successors = task.successors().map(Set::stream).orElseGet(java.util.stream.Stream::empty)
        .filter(t -> t.id() != null)
        .sorted(Comparator.comparing(TaskBean::name, String.CASE_INSENSITIVE_ORDER))
        .toList();
    if (successors.isEmpty())
    {
      TaskUiSupport.showInfo("Remove successor", "Task has no successors.");
      return;
    }
    pickExisting("Remove successor", successors).ifPresent(succ -> {
      try
      {
        taskClient.removePredecessor(succ, task);
        refreshTaskWithRelations();
      }
      catch (Exception e)
      {
        log.error("failed to remove successor", e);
        TaskUiSupport.showError("Remove successor", e);
      }
    });
  }

  private void refreshTaskWithRelations()
  {
    if (task == null || task.id() == null) return;
    TaskBean refreshed = taskClient.findByIdWithRelated(task.id()).orElse(task);
    task(refreshed);
    taskUpdatedListener.accept(refreshed);
  }

  private Optional<TaskBean> pickTask(String title, Set<Long> excludedIds)
  {
    List<TaskBean> choices = taskClient.findAll().stream()
        .filter(t -> t.id() != null && !excludedIds.contains(t.id()))
        .sorted(Comparator.<TaskBean, String>comparing(t -> t.taskGroup().name(), String.CASE_INSENSITIVE_ORDER)
            .thenComparing(TaskBean::name, String.CASE_INSENSITIVE_ORDER))
        .toList();
    if (choices.isEmpty())
    {
      TaskUiSupport.showInfo(title, "No eligible tasks available.");
      return Optional.empty();
    }
    Function<TaskBean, String> label = t -> t.taskGroup().name() + " / " + t.name();

    TextFieldAutoCompleteClearableWithArrowButton<TaskBean> field =
        TextFieldAutoCompleteClearableWithArrowButtonBuilder.<TaskBean>create()
            .items(choices)
            .suggestionFilter((t, text) -> label.apply(t).toLowerCase().contains(text.toLowerCase()))
            .comparator(Comparator.comparing(label))
            .textProvider(label)
            .prompt("Choose or type a task …")
            .build();
    field.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(field, javafx.scene.layout.Priority.ALWAYS);

    Dialog<TaskBean> dlg = new Dialog<>();
    dlg.setTitle(title);
    dlg.setHeaderText(null);
    dlg.getDialogPane().setContent(field);
    dlg.getDialogPane().setPrefWidth(420);
    dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    dlg.setResultConverter(bt -> bt == ButtonType.OK ? field.valueProperty().get() : null);
    return dlg.showAndWait().filter(t -> t != null);
  }

  private Optional<TaskBean> pickExisting(String title, List<TaskBean> tasks)
  {
    java.util.Map<String, TaskBean> byLabel = new java.util.LinkedHashMap<>();
    tasks.forEach(t -> byLabel.put(t.taskGroup().name() + " / " + t.name() + " (#" + t.id() + ")", t));
    List<String> labels = byLabel.keySet().stream().toList();
    ChoiceDialog<String> dlg = new ChoiceDialog<>(labels.get(0), labels);
    dlg.setTitle(title);
    dlg.setHeaderText(null);
    dlg.setContentText("Task:");
    return dlg.showAndWait().map(byLabel::get);
  }

  private void refreshRelationFields()
  {
    tfPredecessors.setText(task == null ? "" : relationNames(task.predecessors()));
    tfSuccessors.setText(task == null ? "" : relationNames(task.successors()));
  }

  private void updateRelationButtons()
  {
    boolean disabled = !editable || task == null || task.id() == null;
    btnAddPredecessor.setDisable(disabled);
    btnRemovePredecessor.setDisable(disabled);
    btnAddSuccessor.setDisable(disabled);
    btnRemoveSuccessor.setDisable(disabled);
  }

  private static String relationNames(Optional<Set<TaskBean>> relations)
  {
    return relations.map(set -> set.stream()
            .map(TaskBean::name)
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .reduce((a, b) -> a + ", " + b)
            .orElse(""))
        .orElse("");
  }

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
    if (editorService == null)
      throw new IllegalStateException("TaskEditorService not bound: call bindEditorService() from TaskEditor.");
    return editorService;
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
