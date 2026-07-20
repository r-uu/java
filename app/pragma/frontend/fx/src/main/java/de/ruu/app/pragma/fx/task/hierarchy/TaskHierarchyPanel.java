package de.ruu.app.pragma.fx.task.hierarchy;

import de.ruu.app.pragma.bean.TaskBean;
import de.ruu.app.pragma.fx.task.edit.TaskEditor;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Reusable UI panel for one hierarchy column (predecessor / center / successor).
 * Encapsulates the TreeView, CRUD buttons, and the task editor component.
 */
class TaskHierarchyPanel
{
  private final VBox root = new VBox(3);
  final TreeView<TaskBean> treeView = new TreeView<>();

  final Button btnAdd;
  final Button btnEdit;
  final Button btnDel;
  final Button btnSave = iconButton("far-save", "Task speichern");

  private final TaskEditor editor;

  TaskHierarchyPanel(String title, boolean rightToLeft, String addTip, String editTip, String delTip, TaskEditor editor)
  {
    this.editor = editor;
    btnAdd = iconButton("far-plus-square", addTip);
    btnEdit = iconButton("far-edit", editTip);
    btnDel = iconButton("far-trash-alt", delTip);

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox titleBar = new HBox(5, new Label(title), spacer, new HBox(3, btnAdd, btnEdit, btnDel, btnSave));
    titleBar.setAlignment(Pos.CENTER_LEFT);

    treeView.setCellFactory(v -> new TreeCell<>()
    {
      @Override protected void updateItem(TaskBean item, boolean empty)
      {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : item.name());
      }
    });
    treeView.setShowRoot(false);
    treeView.setRoot(new TreeItem<>());
    if (rightToLeft) treeView.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
    VBox.setVgrow(treeView, Priority.ALWAYS);

    VBox.setVgrow(editor.localRoot(), Priority.NEVER);

    root.setPadding(new Insets(3, 5, 5, 5));
    root.getChildren().addAll(titleBar, treeView, editor.localRoot());
    clearDetail();
  }

  VBox root() { return root; }
  BooleanProperty dirtyProperty() { return editor.service().dirtyProperty(); }
  void clearDirty() { editor.service().clearDirty(); }

  TaskBean selectedTask()
  {
    TreeItem<TaskBean> item = treeView.getSelectionModel().getSelectedItem();
    return item == null ? null : item.getValue();
  }

  TreeItem<TaskBean> selectedItem() { return treeView.getSelectionModel().getSelectedItem(); }

  void fillDetail(TreeItem<TaskBean> item)
  {
    if (item == null || item.getValue() == null)
    {
      clearDetail();
      return;
    }
    editor.service().task(item.getValue());
    btnSave.setDisable(item.getValue().id() == null);
  }

  void clearDetail()
  {
    editor.service().clear();
    btnSave.setDisable(true);
  }

  void setDisabled(boolean disabled)
  {
    btnAdd.setDisable(disabled);
    btnEdit.setDisable(disabled);
    btnDel.setDisable(disabled);
    btnSave.setDisable(disabled);
    editor.localRoot().setDisable(disabled);
  }

  void applyEditableFields(TaskBean task) { editor.service().applyTo(task); }

  private static Button iconButton(String iconLiteral, String tip)
  {
    Button btn = new Button();
    btn.setGraphic(new FontIcon(iconLiteral));
    btn.setTooltip(new Tooltip(tip));
    btn.setPrefSize(20, 20);
    return btn;
  }
}
