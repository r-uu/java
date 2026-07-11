package de.ruu.lib.fx.control;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class EditableComboBoxTest extends Application
{
	@Override	public void start(Stage stage)
	{
		ComboBox<String> comboBox = new ComboBox<>();
		ObservableList<String> observableList = FXCollections.observableArrayList();
		observableList.add("one");
		observableList.add("two");
		observableList.add("three");
		comboBox.setEditable(true);
		comboBox.setItems(observableList);
		Button button = new Button("print combo box value");
		button.setOnAction(e -> System.out.print(comboBox.getValue()));
		HBox root = new HBox();
		root.getChildren().add(comboBox);
		root.getChildren().add(button);
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}
}