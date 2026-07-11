package de.ruu.lib.ws.rs;

import jakarta.ws.rs.core.Response;

import java.util.Locale;
import java.util.Optional;

public interface ResponseUtil
{
	static boolean isJson(Response response)
	{
		try
		{
			return
					Optional.ofNullable(response.getMediaType()).map(mt -> mt.toString().toLowerCase(Locale.ROOT).contains("json"))
					.orElse(false);
		}
		catch (Exception e) { return false; }
	}
}