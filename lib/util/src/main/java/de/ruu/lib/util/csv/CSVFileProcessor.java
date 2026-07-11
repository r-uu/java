package de.ruu.lib.util.csv;

import java.nio.file.Path;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface CSVFileProcessor
{
	void process(@NonNull Path csvFile);

	CSVFileProcessor csvRowProcessor(@NonNull CSVRowProcessor processor);

	class CSVFileProcessorSimple implements CSVFileProcessor
	{
		static final Logger log = LoggerFactory.getLogger(CSVFileProcessorSimple.class);

		private @NonNull CSVRowProcessor processor = CSVRowProcessor.create();

		/** Public constructor. */
		public CSVFileProcessorSimple() { }

		@Override public void process(@NonNull Path csvFile)
		{
			log.debug("processing file {}", csvFile.toAbsolutePath().toString());
		}

		@Override public CSVFileProcessor csvRowProcessor(@NonNull CSVRowProcessor processor)
		{
			this.processor = processor;
			return this;
		}
	}
	
	static CSVFileProcessor create() { return new CSVFileProcessorSimple(); }
}