package de.ruu.app.pragma.fx;

import de.ruu.app.pragma.fx.task.gantt.Gantt;
import de.ruu.app.pragma.fx.task.graph.Graph;
import de.ruu.app.pragma.fx.task.hierarchy.Hierarchies;
import de.ruu.app.pragma.fx.admin.Admin;
import de.ruu.lib.fx.comp.FXCController.DefaultFXCController;
import de.ruu.lib.postgres.toolbox.ui.PostgresBackupUI;
import de.ruu.lib.postgres.toolbox.ui.PostgresRestoreUI;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Dependent
class PragmaController extends DefaultFXCController<Pragma, PragmaService> implements PragmaService
{
    private static final Logger log = LogManager.getLogger(PragmaController.class);

    @FXML private AnchorPane paneHierarchies;
    @FXML private AnchorPane paneGantt;
    @FXML private AnchorPane paneGraph;
    @FXML private AnchorPane paneAdmin;

    @FXML private Button btnBackup;
    @FXML private Button btnRestore;

    @Inject private Hierarchies     hierarchies;
    @Inject private Gantt           gantt;
    @Inject private Graph           graph;
    @Inject private Admin           admin;
    @Inject private PostgresBackupUI  backupUI;
    @Inject private PostgresRestoreUI restoreUI;

    private Stage backupStage;
    private Stage restoreStage;

    @Override
    @FXML
    protected void initialize()
    {
        embed(paneHierarchies, hierarchies.localRoot());
        embed(paneGantt,       gantt      .localRoot());
        embed(paneGraph,       graph      .localRoot());
        embed(paneAdmin,       admin      .localRoot());

        btnBackup .setOnAction(e -> openBackup ());
        btnRestore.setOnAction(e -> openRestore());
    }

    private void openBackup()
    {
        if (backupStage == null)
        {
            backupStage = new Stage();
            backupStage.setTitle("Backup");
            backupStage.setScene(new Scene(backupUI.localRoot()));
            backupStage.initOwner(btnBackup.getScene().getWindow());
            backupStage.initModality(Modality.NONE);
        }
        backupStage.show();
        backupStage.toFront();
    }

    private void openRestore()
    {
        if (restoreStage == null)
        {
            restoreStage = new Stage();
            restoreStage.setTitle("Restore");
            restoreStage.setScene(new Scene(restoreUI.localRoot()));
            restoreStage.initOwner(btnRestore.getScene().getWindow());
            restoreStage.initModality(Modality.NONE);
        }
        restoreStage.show();
        restoreStage.toFront();
    }

    private void embed(AnchorPane pane, javafx.scene.Node node)
    {
        AnchorPane.setTopAnchor   (node, 0.0);
        AnchorPane.setBottomAnchor(node, 0.0);
        AnchorPane.setLeftAnchor  (node, 0.0);
        AnchorPane.setRightAnchor (node, 0.0);
        pane.getChildren().add(node);
    }
}
