package de.ruu.lib.util;

import org.jspecify.annotations.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import static java.nio.charset.StandardCharsets.UTF_8;

public interface IO
{
	public static String toString(@NonNull InputStream inputStream) throws IOException
	{
		ByteArrayOutputStream result = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];

		int length;

		while ((length = inputStream.read(buffer)) != -1)
		{
			result.write(buffer, 0, length);
		}

		return result.toString(UTF_8);
	}

	public static boolean isListening(@NonNull String host, int port) throws UnknownHostException
	{
		try (Socket socket = new Socket(host, port)) { return socket.isConnected();  }
		catch (IOException e)                        { return false; }
	}

	/**
	 * Captures everything that the given runnable writes to the given PrintStream (e.g. System.out or System.err),
	 * restores the original stream, forwards the captured output back to the original stream, and returns the captured
	 * output as a string.
	 *
	 * @param printStream the PrintStream to capture (e.g. System.out or System.err)
	 * @param runnable    the code to execute
	 * @return            the captured output as string
	 */
	public static String capturePrintStreamOutputOfRunnable(@NonNull PrintStream printStream, @NonNull Runnable runnable)
	{
		PrintStream original = printStream; // save original stream

		// capture output in memory
		ByteArrayOutputStream capturingStreamAsBinaryArrayOutputStream =
				new ByteArrayOutputStream();
		PrintStream           capturingStreamAsPrintStream             =
				new PrintStream(capturingStreamAsBinaryArrayOutputStream, true);

		try
		{
			// temporarily redirect System.out or System.err if needed
			if      (printStream == System.out) System.setOut(capturingStreamAsPrintStream);
			else if (printStream == System.err) System.setErr(capturingStreamAsPrintStream);
			// for custom PrintStream, assign capturing stream
			else                                printStream = capturingStreamAsPrintStream;

			// execute the runnable, capturing everything it writes to the p stream
			runnable.run();
		}
		finally
		{
			// restore original stream
			if      (original == System.out) System.setOut(original);
			else if (original == System.err) System.setErr(original);
		}

		String result = capturingStreamAsBinaryArrayOutputStream.toString();

		// forward result output to the original stream
		original.print(result);

		return result;
	}
}