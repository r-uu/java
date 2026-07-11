package de.ruu.lib.fx;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Optional;

public class FileChooserUtil
{
	public static Optional<File> chooseFile(Window ownerWindow, String title, File initialDirectory, String... extensions)
	{
		if (initialDirectory != null && !initialDirectory.isDirectory())
		{
			throw new IllegalArgumentException("initial directory must be a valid directory.");
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.setInitialDirectory(initialDirectory);

		if (extensions != null && extensions.length > 0)
		{
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("allowed files", extensions));
		}

		File selectedFile = fileChooser.showOpenDialog(ownerWindow);
		return Optional.ofNullable(selectedFile);
	}
}