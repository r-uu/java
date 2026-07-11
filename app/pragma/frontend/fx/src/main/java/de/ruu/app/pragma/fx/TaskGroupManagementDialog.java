package de.ruu.app.pragma.fx;

import de.ruu.app.pragma.bean.TaskGroupBean;
import de.ruu.app.pragma.client.TaskGroupClient;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Modal dialog for managing task groups (create, rename, delete).
 * Shared by all tab controllers.
 */
public class TaskGroupManagementDialog
{
    private static final Logger log = LoggerFactory.getLogger(TaskGroupManagementDialog.class);

    private final TaskGroupClient client;
    private final Runnable        onGroupsChanged;

    private Stage                   stage;
    private ListView<TaskGroupBean> listView;

    public TaskGroupManagementDialog(TaskGroupClient client, Runnable onGroupsChanged)
    {
        this.client          = client;
        this.onGroupsChanged = onGroupsChanged;
    }

    public void showAndWait()
    {
        if (stage == null) buildStage();
        reload();
        stage.showAndWait();
        onGroupsChanged.run();
    }

    private void buildStage()
    {
        listView = new ListView<>();
        listView.setCellFactory(lv -> new ListCell<>()
        {
            @Override protected void updateItem(TaskGroupBean item, boolean empty)
            {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name());
            }
        });
        VBox.setVgrow(listView, Priority.ALWAYS);

        Button btnAdd    = iconButton("far-plus-square", "Gruppe hinzufügen");
        Button btnRename = iconButton("far-edit",        "Gruppe umbenennen");
        Button btnDelete = iconButton("far-trash-alt",   "Gruppe löschen");

        btnAdd   .setOnAction(e -> onAdd());
        btnRename.setOnAction(e -> onRename());
        btnDelete.setOnAction(e -> onDelete());

        listView.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            boolean noSel = sel == null;
            btnRename.setDisable(noSel);
            btnDelete.setDisable(noSel);
        });
        btnRename.setDisable(true);
        btnDelete.setDisable(true);

        HBox toolbar = new HBox(4, btnAdd, btnRename, btnDelete);
        toolbar.setPadding(new Insets(4));

        VBox root = new VBox(6, listView, toolbar);
        root.setPadding(new Insets(8));

        stage = new Stage();
        stage.setTitle("Task Groups verwalten");
        stage.setScene(new Scene(root, 320, 380));
        stage.initModality(Modality.APPLICATION_MODAL);
    }

    private void reload()
    {
        try
        {
            List<TaskGroupBean> groups = client.findAll();
            listView.getItems().setAll(groups);
        }
        catch (Exception e) { log.error("failed to load groups", e); }
    }

    private void onAdd()
    {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Gruppe hinzufügen");
        dlg.setHeaderText(null);
        dlg.setContentText("Name:");
        dlg.showAndWait()
           .map(String::trim).filter(s -> !s.isEmpty())
           .ifPresent(name -> {
               try { client.create(new TaskGroupBean(name)); reload(); }
               catch (Exception e) { log.error("failed to create group", e); showError("Gruppe anlegen", e); }
           });
    }

    private void onRename()
    {
        TaskGroupBean sel = listView.getSelectionModel().getSelectedItem();
        if (sel == null || sel.id() == null) return;

        TextInputDialog dlg = new TextInputDialog(sel.name());
        dlg.setTitle("Gruppe umbenennen");
        dlg.setHeaderText(null);
        dlg.setContentText("Name:");
        dlg.showAndWait()
           .map(String::trim).filter(s -> !s.isEmpty() && !s.equals(sel.name()))
           .ifPresent(name -> {
               try { sel.name(name); client.update(sel); reload(); }
               catch (Exception e) { log.error("failed to rename group", e); showError("Gruppe umbenennen", e); }
           });
    }

    private void onDelete()
    {
        TaskGroupBean sel = listView.getSelectionModel().getSelectedItem();
        if (sel == null || sel.id() == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Gruppe \"" + sel.name() + "\" löschen?\n(Alle Tasks der Gruppe werden ebenfalls gelöscht.)",
            ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Gruppe löschen");
        confirm.setHeaderText(null);
        if (confirm.showAndWait().filter(bt -> bt == ButtonType.OK).isPresent())
        {
            try { client.delete(sel); reload(); }
            catch (Exception e) { log.error("failed to delete group", e); showError("Gruppe löschen", e); }
        }
    }

    private static Button iconButton(String iconLiteral, String tip)
    {
        Button btn = new Button();
        btn.setGraphic(new FontIcon(iconLiteral));
        btn.setTooltip(new Tooltip(tip));
        btn.setPrefSize(24, 24);
        return btn;
    }

    private static void showError(String title, Exception e)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
