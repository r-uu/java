package de.ruu.lib.fx.control.textfield.number;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

import static java.util.Objects.isNull;

public class IntegerTextFieldConfiguratorDemo extends Application
{
	private static final Logger log = LoggerFactory.getLogger(IntegerTextFieldConfiguratorDemo.class);

	@Override	public void start(Stage stage)
	{
		NumberTextFieldPostConvertAction postConvertAction =
				(txtFld, xcptn) ->
				{
					log.debug("post convert, xcptn == null: " + (xcptn == null));
				};
		Predicate<Integer> predicate =
				(integer) ->
				{
					if (isNull(integer))
					{
						log.debug("predicate, integer > 0: false");
						return false;
					}
					log.debug("predicate, integer > 0: " + (integer > 0));
					return integer > 0;
				};
		TextFieldPostValidateAction postValidateAction =
				(txtFld, bool) ->
				{
					log.debug("post valid, bool: " + bool);
				};

		TextField textField = new TextField("0");
		NumberTextFieldConfigurator.configureIntegerTextField(textField, predicate, postConvertAction, postValidateAction);

		HBox root = new HBox();
		root.getChildren().add(textField);

		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.show();

		HBox.setHgrow(root, Priority.ALWAYS);

		textField.setMaxWidth(Double.MAX_VALUE);
		textField.setMinWidth(textField.getWidth());
		textField.setPrefWidth(textField.getWidth());

		textField.requestFocus();
	}
}