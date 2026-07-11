package de.ruu.lib.fx.control.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.PrintWriter;
import java.io.StringWriter;

import static java.lang.Double.MAX_VALUE;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.layout.Priority.ALWAYS;
import static org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TIMES_CIRCLE;

public abstract class ExceptionDialog
{
	public static void showAndWait(String title, String header, String content, Exception e)
	{
		Alert alert = new Alert(ERROR);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.setGraphic(FontIcon.of(TIMES_CIRCLE, 48, Color.web("#e74c3c")));

		// Create expandable Exception.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String exceptionText = sw.toString();

		Label label = new Label("exception stacktrace:");

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth (MAX_VALUE);
		textArea.setMaxHeight(MAX_VALUE);
		GridPane.setVgrow(textArea, ALWAYS);
		GridPane.setHgrow(textArea, ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent(expContent);

		alert.showAndWait();
	}

	public static void showAndWait(String content, Exception e)
	{
		showAndWait(e.getClass().getSimpleName(), e.getClass().getName() + " occurred", content, e);
	}

	public static void showAndWait(Exception e)
	{
		showAndWait(
				e.getClass().getSimpleName(),
				e.getClass().getName() + " occurred", "an error occurred. Please check the stack trace for details.",
				e);
	}
}
