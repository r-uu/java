package de.ruu.lib.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Utility class for automatic WSL2 IP detection and property resolution.
 *
 * <p>Automatically resolves {@code ${WSL2_IP}} placeholders in property values with the actual
 * WSL2 IP address. This allows configuration files to work on different machines without manual
 * adjustment.
 *
 * <p>Usage:
 * <pre>
 * Properties props = Wsl2IpResolver.loadAndResolve("config.properties");
 * String dbHost = props.getProperty("db.host"); // Returns actual IP like "172.25.188.232"
 * </pre>
 */
public class Wsl2IpResolver
{
	private static final Logger log = LoggerFactory.getLogger(Wsl2IpResolver.class);

	/** Private constructor - utility class should not be instantiated */
	private Wsl2IpResolver()
	{
		throw new UnsupportedOperationException("Utility class - do not instantiate");
	}

	private static final String WSL2_IP_PLACEHOLDER = "${WSL2_IP}";

	private static volatile String cachedWsl2Ip = null;

	/**
	 * Get the current WSL2 IP address.
	 * Result is cached for performance.
	 *
	 * @return WSL2 IP address or "localhost" if detection fails
	 */
	public static String wsl2Ip()
	{
		if (cachedWsl2Ip != null)
		{
			return cachedWsl2Ip;
		}

		try
		{
			// Try to get IP via hostname -I command (most reliable for WSL2)
			String ipFromCommand = getIpFromCommand();
			if (ipFromCommand != null && !ipFromCommand.isEmpty())
			{
				cachedWsl2Ip = ipFromCommand;
				log.debug("WSL2 IP detected via command: {}", cachedWsl2Ip);
				return cachedWsl2Ip;
			}

			// Fallback: Try to get IP via NetworkInterface
			String ipFromNetwork = getIpFromNetworkInterface();
			if (ipFromNetwork != null && !ipFromNetwork.isEmpty())
			{
				cachedWsl2Ip = ipFromNetwork;
				log.debug("WSL2 IP detected via NetworkInterface: {}", cachedWsl2Ip);
				return cachedWsl2Ip;
			}

			// Fallback: use localhost
			log.warn("Could not detect WSL2 IP, falling back to localhost");
			cachedWsl2Ip = "localhost";
			return cachedWsl2Ip;
		}
		catch (Exception e)
		{
			log.error("Error detecting WSL2 IP", e);
			return "localhost";
		}
	}

	/**
	 * Get IP address by executing hostname -I command.
	 */
	private static String getIpFromCommand()
	{
		try
		{
			ProcessBuilder pb = new ProcessBuilder("sh", "-c", "hostname -I | awk '{print $1}'");
			Process process = pb.start();

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
			{
				String ip = reader.readLine();
				process.waitFor();

				if (ip != null && !ip.trim().isEmpty() && !ip.equals("127.0.0.1"))
				{
					return ip.trim();
				}
			}
		}
		catch (Exception e)
		{
			log.debug("Could not get IP from command", e);
		}
		return null;
	}

	/**
	 * Get IP address via NetworkInterface (fallback method).
	 */
	private static String getIpFromNetworkInterface()
	{
		try
		{
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements())
			{
				NetworkInterface networkInterface = interfaces.nextElement();

				// Skip loopback and inactive interfaces
				if (networkInterface.isLoopback() || !networkInterface.isUp())
				{
					continue;
				}

				Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
				while (addresses.hasMoreElements())
				{
					InetAddress address = addresses.nextElement();

					// Get first non-loopback IPv4 address
					if (!address.isLoopbackAddress() && address.getHostAddress().contains("."))
					{
						return address.getHostAddress();
					}
				}
			}
		}
		catch (Exception e)
		{
			log.debug("Could not get IP from NetworkInterface", e);
		}
		return null;
	}

	/**
	 * Load properties from file and resolve ${WSL2_IP} placeholders.
	 *
	 * @param filename Path to properties file
	 * @return Properties with resolved WSL2_IP placeholders
	 * @throws IOException if file cannot be read
	 */
	public static Properties loadAndResolve(String filename) throws IOException
	{
		Properties props = new Properties();

		try (InputStream input = new FileInputStream(filename))
		{
			props.load(input);
		}

		return resolve(props);
	}

	/**
	 * Resolve ${WSL2_IP} placeholders in all property values.
	 *
	 * @param properties Properties to resolve
	 * @return Properties with resolved placeholders (same instance, modified)
	 */
	public static Properties resolve(Properties properties)
	{
		String wsl2Ip = wsl2Ip();

		for (String key : properties.stringPropertyNames())
		{
			String value = properties.getProperty(key);
			if (value != null && value.contains(WSL2_IP_PLACEHOLDER))
			{
				String resolved = value.replace(WSL2_IP_PLACEHOLDER, wsl2Ip);
				properties.setProperty(key, resolved);
				log.debug("Resolved property {}: {} -> {}", key, value, resolved);
			}
		}

		return properties;
	}

	/**
	 * Clear cached WSL2 IP (useful for testing or if IP changes).
	 */
	public static void clearCache()
	{
		cachedWsl2Ip = null;
		log.debug("WSL2 IP cache cleared");
	}
}

