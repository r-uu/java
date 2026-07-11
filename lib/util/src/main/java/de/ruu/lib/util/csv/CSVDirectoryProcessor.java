package de.ruu.lib.util.csv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes all csv files in a directory. Processing is configured by processors that can be provided by {@link
 * #csvFileProcessor(CSVFileProcessor)}.
 */
public interface CSVDirectoryProcessor
{
	void process(@NonNull Path csvDirectory);

	CSVDirectoryProcessor csvFileProcessor(@NonNull CSVFileProcessor processor);

	class CSVDirectoryProcessorSimple implements CSVDirectoryProcessor
	{
		static final Logger log = LoggerFactory.getLogger(CSVDirectoryProcessorSimple.class);

		@NonNull private CSVFileProcessor csvFileProcessor = CSVFileProcessor.create();

		/** Public constructor. */
		public CSVDirectoryProcessorSimple() { }

		public CSVFileProcessor csvFileProcessor() { return csvFileProcessor; }

		@Override public CSVDirectoryProcessorSimple csvFileProcessor(@NonNull CSVFileProcessor csvFileProcessor)
		{
			this.csvFileProcessor = csvFileProcessor;
			return this;
		}

		@Override public void process(@NonNull Path csvDirectory)
		{
			try (Stream<Path> paths = Files.list(csvDirectory))
			{
				log.debug("processing directory {}", csvDirectory);
				paths.forEach(path -> csvFileProcessor.process(path));
				log.debug("processed  directory {}", csvDirectory);
			}
			catch (IOException e)
			{
				throw new RuntimeException("failure listing files in " + csvDirectory, e);
			}
		}
	}

	static CSVDirectoryProcessor create() { return new CSVDirectoryProcessorSimple(); }
}
