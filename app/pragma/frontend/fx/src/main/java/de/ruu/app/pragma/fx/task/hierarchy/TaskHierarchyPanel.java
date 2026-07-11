package de.ruu.app.pragma.fx.task.hierarchy;

import de.ruu.app.pragma.bean.TaskBean;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Reusable UI panel for one hierarchy column (predecessor / center / successor).
 * Encapsulates the TreeView, CRUD buttons, and the task detail form.
 */
class TaskHierarchyPanel
{
    private final VBox root = new VBox(3);

    final TreeView<TaskBean> treeView = new TreeView<>();

    final Button btnAdd;
    final Button btnEdit;
    final Button btnDel;
    final Button btnSave  = iconButton("far-save", "Datumfelder speichern");

    final TextField  tfId     = readOnlyField();
    final TextField  tfName   = readOnlyField();
    final DatePicker dpStart  = new DatePicker();
    final DatePicker dpEnd    = new DatePicker();
    final TextArea   taDesc   = new TextArea();
    final CheckBox   cbClosed = new CheckBox();

    private final BooleanProperty dirty    = new SimpleBooleanProperty(false);
    private       boolean         updating = false;

    TaskHierarchyPanel(String title, boolean rightToLeft,
                       String addTip, String editTip, String delTip)
    {
        btnAdd  = iconButton("far-plus-square", addTip);
        btnEdit = iconButton("far-edit",        editTip);
        btnDel  = iconButton("far-trash-alt",   delTip);

        // ── title bar ────────────────────────────────────────────────────────
        Region spacer  = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox titleBar = new HBox(5, new Label(title), spacer, new HBox(3, btnAdd, btnEdit, btnDel));
        titleBar.setAlignment(Pos.CENTER_LEFT);

        // ── tree view ────────────────────────────────────────────────────────
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

        // ── detail grid ──────────────────────────────────────────────────────
        taDesc.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(3);

        ColumnConstraints col0 = new ColumnConstraints();
        col0.setMinWidth(22); col0.setMaxWidth(22);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHalignment(HPos.RIGHT); col1.setHgrow(Priority.NEVER); col1.setMinWidth(95);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col0, col1, col2);

        grid.add(btnSave,                       0, 0);
        grid.add(new Label("id"),               1, 0); grid.add(tfId,    2, 0);
        grid.add(new Label("name"),             1, 1); grid.add(tfName,  2, 1);
        grid.add(new Label("planned start"),    1, 2); grid.add(dpStart, 2, 2);
        grid.add(new Label("planned end"),      1, 3); grid.add(dpEnd,   2, 3);
        grid.add(new Label("description"),      1, 4); grid.add(taDesc,  2, 4);
        grid.add(new Label("closed"),           1, 5); grid.add(cbClosed,2, 5);

        GridPane.setFillWidth(tfId,    true);
        GridPane.setFillWidth(tfName,  true);
        GridPane.setFillWidth(dpStart, false);
        GridPane.setFillWidth(dpEnd,   false);
        GridPane.setFillWidth(taDesc,  true);
        GridPane.setVgrow    (taDesc,  Priority.ALWAYS);

        // dirty tracking: user edits → dirty = true (suppressed during programmatic fill)
        dpStart  .valueProperty()   .addListener((obs, o, n) -> { if (!updating) dirty.set(true); });
        dpEnd    .valueProperty()   .addListener((obs, o, n) -> { if (!updating) dirty.set(true); });
        taDesc   .textProperty()    .addListener((obs, o, n) -> { if (!updating) dirty.set(true); });
        cbClosed .selectedProperty().addListener((obs, o, n) -> { if (!updating) dirty.set(true); });

        root.setPadding(new Insets(3, 5, 5, 5));
        root.getChildren().addAll(titleBar, treeView, grid);
    }

    VBox root() { return root; }

    BooleanProperty dirtyProperty() { return dirty; }

    TaskBean selectedTask()
    {
        TreeItem<TaskBean> item = treeView.getSelectionModel().getSelectedItem();
        return item == null ? null : item.getValue();
    }

    TreeItem<TaskBean> selectedItem()
    {
        return treeView.getSelectionModel().getSelectedItem();
    }

    void fillDetail(TreeItem<TaskBean> item)
    {
        updating = true;
        try
        {
            if (item == null || item.getValue() == null)
            {
                clearDetailFields();
                return;
            }
            TaskBean t = item.getValue();
            tfId    .setText(t.id()   == null ? "" : t.id().toString());
            tfName  .setText(t.name() == null ? "" : t.name());
            dpStart .setValue(t.plannedStart().orElse(null));
            dpEnd   .setValue(t.plannedEnd()  .orElse(null));
            taDesc  .setText(t.description().orElse(""));
            cbClosed.setSelected(t.closed());
            btnSave .setDisable(t.id() == null);
        }
        finally
        {
            updating = false;
            dirty.set(false);
        }
    }

    void clearDetail()
    {
        updating = true;
        try   { clearDetailFields(); }
        finally { updating = false; }
    }

    void setDisabled(boolean disabled)
    {
        btnAdd .setDisable(disabled);
        btnEdit.setDisable(disabled);
        btnDel .setDisable(disabled);
        btnSave.setDisable(disabled);
    }

    private void clearDetailFields()
    {
        tfId    .clear();
        tfName  .clear();
        dpStart .setValue(null);
        dpEnd   .setValue(null);
        taDesc  .clear();
        cbClosed.setSelected(false);
        btnSave .setDisable(true);
        dirty.set(false);
    }

    private static Button iconButton(String iconLiteral, String tip)
    {
        Button btn = new Button();
        btn.setGraphic(new FontIcon(iconLiteral));
        btn.setTooltip(new Tooltip(tip));
        btn.setPrefSize(20, 20);
        return btn;
    }

    private static TextField readOnlyField()
    {
        TextField tf = new TextField();
        tf.setEditable(false);
        tf.setStyle("-fx-background-color: transparent;");
        return tf;
    }
}
