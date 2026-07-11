package de.ruu.lib.util.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes all files in a directory. Processing is configured by processors that can be provided by {@link
 * #bufferedFileRowsProcessor(CSVFileProcessor)}.
 */
public interface DirectoryEntryProvider
{
	Stream<Path> provide(@NonNull Path directory);
	Stream<Path> provide(@NonNull Path directory, @NonNull Predicate<Path> predicate);

	class DirectoryEntryProviderSimple implements DirectoryEntryProvider
	{
		static final Logger log = LoggerFactory.getLogger(DirectoryEntryProviderSimple.class);

		/** Public constructor. */
		public DirectoryEntryProviderSimple() { }

		@Override public Stream<Path> provide(@NonNull Path directory)
		{
			return provide(directory, (p) -> true);
		}

		@Override public Stream<Path> provide(@NonNull Path directory, Predicate<Path> predicate)
		{
			log.debug("providing entries for directory {}", directory);

			if (directory.toFile().isDirectory() == false) throw new IllegalArgumentException(directory + " is not a directory");
			if (directory.toFile().exists     () == false) throw new IllegalArgumentException(directory + " does not exist");

			List<Path> result = new ArrayList<Path>();

			try (Stream<Path> paths = Files.list(directory))
			{
				paths.forEach
				(
						path ->
						{
							if (predicate.test(path)) result.add(path);
						}
				);
			}
			catch (IOException e)
			{
				throw new RuntimeException("failure listing files in " + directory, e);
			}

			return result.stream();
		}
	}

	static DirectoryEntryProvider create() { return new DirectoryEntryProviderSimple(); }
}
