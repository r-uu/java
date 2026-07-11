package de.ruu.lib.ws.rs;

public class NonTechnicalException extends RuntimeException
{
	private final ErrorResponse errorResponse;

	public NonTechnicalException(ErrorResponse errorResponse)
	{
		super(errorResponse.message());
		this.errorResponse = errorResponse;
	}

	public ErrorResponse getErrorResponse() { return errorResponse; }
}
