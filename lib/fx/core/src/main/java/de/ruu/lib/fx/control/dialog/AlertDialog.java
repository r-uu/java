package de.ruu.lib.fx.control.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import static org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CHECK_CIRCLE;
import static org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.EXCLAMATION_TRIANGLE;
import static org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.INFO_CIRCLE;
import static org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TIMES_CIRCLE;

public abstract class AlertDialog
{
	public static void showAndWait(String content)                 { showAndWait(content,              INFORMATION); }
	public static void showAndWait(String content, AlertType type) { showAndWait(""     , "", content, type       ); }
	public static void showAndWait(String title, String content)   { showAndWait(title  , "", content, INFORMATION); }

	public static void showAndWait(String title, String header, String content, AlertType type)
	{
		Alert alert = new Alert(type);
		alert.setContentText(content);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setResizable(true);
		alert.getDialogPane().setMinHeight(USE_COMPUTED_SIZE);
		alert.showAndWait();

		// icon based on type
		FontIcon icon =
				switch (type)
				{
					case ERROR        -> FontIcon.of(TIMES_CIRCLE        , 48, Color.web("#e74c3c"));
					case WARNING      -> FontIcon.of(EXCLAMATION_TRIANGLE, 48, Color.web("#f39c12"));
					case INFORMATION  -> FontIcon.of(INFO_CIRCLE         , 48, Color.web("#3498db"));
					case CONFIRMATION -> FontIcon.of(CHECK_CIRCLE        , 48, Color.web("#2ecc71"));
					default           -> null;
				};

		alert.setGraphic(icon);
	}

	public static boolean showConfirmAndWait(String title, String header, String content)
	{
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setContentText(content);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setResizable(true);
		alert.getDialogPane().setMinHeight(USE_COMPUTED_SIZE);

		// Set graphic based on CONFIRMATION type
		FontIcon icon = FontIcon.of(CHECK_CIRCLE, 48, Color.web("#2ecc71"));
		alert.setGraphic(icon);

		return alert.showAndWait().filter(buttonType -> buttonType == ButtonType.OK).isPresent();
	}
}