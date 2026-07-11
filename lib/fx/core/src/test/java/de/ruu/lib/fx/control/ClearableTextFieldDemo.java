package de.ruu.lib.fx.control;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClearableTextFieldDemo extends Application
{
    @Override public void start(Stage primaryStage)
    {
        ClearableTextField clearableTextField = new ClearableTextField();
        clearableTextField.prompt("Suchtext...");

        VBox root = new VBox(10, clearableTextField);
        root.setStyle("-fx-padding: 20;");

        Scene scene = new Scene(root, 300, 100);
        primaryStage.setScene(scene);
        primaryStage.setTitle("ClearableTextField Demo");
        primaryStage.show();
    }

    public static void main(String[] args) { launch(args); }
}