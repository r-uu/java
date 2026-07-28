package de.ruu.app.pragma.fx.task;

import jakarta.enterprise.inject.Vetoed;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

@Vetoed
public final class TaskUiSupport
{
  private TaskUiSupport() { }

  public static void showConnecting(Label statusLabel)
  {
    if (statusLabel != null) statusLabel.setText("Connecting ...");
  }

  public static void clearStatus(Label statusLabel)
  {
    if (statusLabel != null) statusLabel.setText("");
  }

  public static void showConnectionError(Label statusLabel)
  {
    if (statusLabel != null) statusLabel.setText("[WARN] Connection error - is the server reachable?");
  }

  public static boolean confirmDiscardChanges(boolean dirty)
  {
    if (!dirty) return true;
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
        "There are unsaved changes. Discard them?",
        ButtonType.OK, ButtonType.CANCEL);
    confirm.setTitle("Unsaved changes");
    confirm.setHeaderText(null);
    return confirm.showAndWait().filter(bt -> bt == ButtonType.OK).isPresent();
  }

  public static void showError(String title, Exception e)
  {
    Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.showAndWait();
  }

  public static void showWarning(String title, String message)
  {
    Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.showAndWait();
  }

  public static void showInfo(String title, String message)
  {
    Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.showAndWait();
  }
}
