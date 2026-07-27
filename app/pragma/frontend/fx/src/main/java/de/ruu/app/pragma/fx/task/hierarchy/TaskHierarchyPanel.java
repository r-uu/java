package de.ruu.app.pragma.fx.task.hierarchy;

import de.ruu.app.pragma.bean.TaskBean;
import de.ruu.app.pragma.core.TaskPriority;
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
 * Encapsulates the TreeView and CRUD buttons; the shared detail inspector lives below
 * the three columns in the parent view.
 */
class TaskHierarchyPanel
{
  private final VBox root = new VBox(3);
  final TreeView<TaskBean> treeView = new TreeView<>();

  final Button btnAdd;
  final Button btnEdit;
  final Button btnDel;

  TaskHierarchyPanel(String title, boolean rightToLeft, String addTip, String editTip, String delTip)
  {
    btnAdd = iconButton("far-plus-square", addTip);
    btnEdit = iconButton("far-edit", editTip);
    btnDel = iconButton("far-trash-alt", delTip);

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox titleBar = new HBox(5, new Label(title), spacer, new HBox(3, btnAdd, btnEdit, btnDel));
    titleBar.setAlignment(Pos.CENTER_LEFT);

    treeView.setCellFactory(v -> new TreeCell<>()
    {
      @Override protected void updateItem(TaskBean item, boolean empty)
      {
        super.updateItem(item, empty);
        if (empty || item == null)
        {
          setText(null);
          setGraphic(null);
          return;
        }

        // Show the task name together with its priority so the hierarchy stays compact
        // while still exposing the attribute directly in the tree.
        Label name = new Label(item.name());
        Label priority = new Label(priorityText(item.priority()));
        priority.setStyle("-fx-background-color: #e8eefc; -fx-text-fill: #1f3a93; "
            + "-fx-padding: 0 6 0 6; -fx-background-radius: 8; -fx-font-size: 10px;");

        HBox row = new HBox(6, name, priority);
        row.setAlignment(Pos.CENTER_LEFT);
        setText(null);
        setGraphic(row);
      }
    });
    treeView.setShowRoot(false);
    treeView.setRoot(new TreeItem<>());
    if (rightToLeft) treeView.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
    VBox.setVgrow(treeView, Priority.ALWAYS);

    root.setPadding(new Insets(3, 5, 5, 5));
    root.getChildren().addAll(titleBar, treeView);
  }

  VBox root() { return root; }

  TaskBean selectedTask()
  {
    TreeItem<TaskBean> item = treeView.getSelectionModel().getSelectedItem();
    return item == null ? null : item.getValue();
  }

  TreeItem<TaskBean> selectedItem() { return treeView.getSelectionModel().getSelectedItem(); }

  void setDisabled(boolean disabled)
  {
    btnAdd.setDisable(disabled);
    btnEdit.setDisable(disabled);
    btnDel.setDisable(disabled);
  }

  private static Button iconButton(String iconLiteral, String tip)
  {
    Button btn = new Button();
    btn.setGraphic(new FontIcon(iconLiteral));
    btn.setTooltip(new Tooltip(tip));
    btn.setPrefSize(20, 20);
    return btn;
  }

  private static String priorityText(TaskPriority priority)
  {
    return (priority == null ? TaskPriority.NORMAL : priority).toString();
  }
}
