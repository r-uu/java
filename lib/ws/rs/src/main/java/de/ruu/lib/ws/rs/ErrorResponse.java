package de.ruu.lib.ws.rs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.ws.rs.core.Response.Status;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse
{
	private final @NonNull String message;
	private final @NonNull String cause;
	private final          Status httpStatus;

	// no-arg constructor for deserialization
	protected ErrorResponse()                                                    { this(""     , ""   , INTERNAL_SERVER_ERROR); }

	public ErrorResponse(@NonNull String message)                                { this(message, ""   , INTERNAL_SERVER_ERROR); }
	public ErrorResponse(@NonNull String message, @NonNull String cause)         { this(message, cause, INTERNAL_SERVER_ERROR); }
	public ErrorResponse(@NonNull String message, @NonNull String cause, Status httpStatus)
	{
		this.message    = Objects.requireNonNull(message, "message");
		this.cause      = Objects.requireNonNull(cause,   "cause");
		this.httpStatus = httpStatus;
	}

	public @NonNull String message()    { return message; }
	public @NonNull String cause()      { return cause; }
	public          Status httpStatus() { return httpStatus; }
}
