package de.ruu.lib.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.net.ServerSocket;

import org.junit.jupiter.api.Test;

class IOTest
{
	@Test void testToString() throws Exception
	{
		String               input  = "input";
		ByteArrayInputStream stream = new ByteArrayInputStream(input.getBytes());
		String               output = IO.toString(stream);

		assertThat(output).isEqualTo(input);
	}

	@Test void testIsListeningTrue() throws Exception
	{
		try (ServerSocket serverSocket = new ServerSocket(0))
		{
			int port = serverSocket.getLocalPort();
			assertThat(IO.isListening("localhost", port)).isEqualTo(true);
		}
	}

	@Test void testCapturePrintStreamOutputOfRunnableWithSystemOut()
	{
		String input  = "input";
		String output = IO.capturePrintStreamOutputOfRunnable(System.out, () ->  System.out.print(input));
		assertThat(output).contains(input);
	}
}