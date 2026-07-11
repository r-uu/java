package de.ruu.lib.util.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jspecify.annotations.NonNull;

public interface BufferedFileRowsProvider
{
	/**
	 * Reads {@code file} line by line and returns all those lines ({@link String}s) for which {@code selector} returns {@code
	 * true}.
	 *
	 * @param file     see above
	 * @param selector see above
	 * @return see above
	 */
	List<String> process(@NonNull Path file, @NonNull Predicate<String> selector);

	class BufferedFileRowsProcessorSimple implements BufferedFileRowsProvider
	{
		/** Public constructor. */
		public BufferedFileRowsProcessorSimple() { }

		@Override public List<String> process(@NonNull Path file, Predicate<String> selector)
		{
			List<String> result = new ArrayList<>();

			try (BufferedReader reader = new BufferedReader(new FileReader(file.toString())))
			{
				String row = reader.readLine(); // read first line
				
				while (row != null)
				{
					if (selector.test(row))
					{
						result.add(row);
					}
					row = reader.readLine();    // read subsequent lines
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException("failure reading file for " + file, e);
			}
			
			return result;
		}
	}

	static BufferedFileRowsProvider create() { return new BufferedFileRowsProcessorSimple(); }
}