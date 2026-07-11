package de.ruu.lib.fx;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Optional;

public class DirectoryChooserUtil
{
	/**
	 * Opens a directory chooser and returns the selected directory as Optional<File>.
	 *
	 * @param ownerWindow the owner window (usually your primary stage)
	 * @param title       the window title
	 * @return Optional containing the chosen directory, or empty if cancelled
	 */
	public static Optional<File> chooseDirectory(Window ownerWindow, String title, File initialDirectory)
	{
		if (initialDirectory != null && !initialDirectory.isDirectory())
		{
			throw new IllegalArgumentException("initial directory must be a valid directory.");
		}

		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle(title);
		directoryChooser.setInitialDirectory(initialDirectory);

		File selectedDirectory = directoryChooser.showDialog(ownerWindow);
		return Optional.ofNullable(selectedDirectory);
	}
}