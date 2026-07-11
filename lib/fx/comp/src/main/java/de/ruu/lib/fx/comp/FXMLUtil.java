package de.ruu.lib.fx.comp;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

abstract class FXMLUtil
{
	private static final Logger log = LoggerFactory.getLogger(FXMLUtil.class);

	static Parent loadFrom(final FXMLLoader loader)
	{
		// load from fxmlLoader
		try
		{
			final Object object = loader.load();

			if (object instanceof Parent)
			{
				log.debug("loaded {} object from {}", Parent.class.getName(), loader.getLocation().toExternalForm());
				return (Parent) object;
			}
			else
			{
				throw new IllegalStateException(
						"unexpected " + object.getClass() + " loaded from " + loader.getLocation().toExternalForm());
			}
		}
		catch (final IOException e)
		{
			throw new IllegalStateException("failure loading object from " + loader.getClass(), e);
		}
	}
}
