package de.ruu.lib.util.config.mp;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.Optional;

public abstract class ConfigSourceUtil
{
	public static Optional<WritableFileConfigSource> activeWritableFileConfigSource()
	{
		for (ConfigSource configSource : ConfigProvider.getConfig().getConfigSources())
		{
			if (configSource instanceof WritableFileConfigSource writableFileConfigSource)
					return Optional.of(writableFileConfigSource);
		}
		return Optional.empty();
	}
}