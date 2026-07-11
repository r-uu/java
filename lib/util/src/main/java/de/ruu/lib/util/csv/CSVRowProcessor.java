package de.ruu.lib.util.csv;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface CSVRowProcessor
{
	void process(@NonNull String row);

	class CSVRowProcessorSimple implements CSVRowProcessor
	{
		static final Logger log = LoggerFactory.getLogger(CSVRowProcessorSimple.class);

		/** Public constructor. */
		public CSVRowProcessorSimple() { }

		@Override public void process(@NonNull String row) { log.debug("processing row {}", row); }
	}
	
	static CSVRowProcessor create() { return new CSVRowProcessorSimple(); }
}