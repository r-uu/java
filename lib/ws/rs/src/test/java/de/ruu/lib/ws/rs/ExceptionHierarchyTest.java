package de.ruu.lib.ws.rs;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionHierarchyTest
{
  @Test
  void technicalExceptionCarriesMessage()
  {
    TechnicalException ex = new TechnicalException("technical failure");
    assertThat(ex.getMessage()).isEqualTo("technical failure");
  }

  @Test
  void technicalExceptionCarriesMessageAndCause()
  {
    RuntimeException cause = new RuntimeException("root");
    TechnicalException ex = new TechnicalException("technical failure", cause);
    assertThat(ex.getMessage()).isEqualTo("technical failure");
    assertThat(ex.getCause()).isSameAs(cause);
  }

  @Test
  void nonTechnicalExceptionWrapsErrorResponse()
  {
    ErrorResponse errorResponse = new ErrorResponse("non-technical error");
    NonTechnicalException ex = new NonTechnicalException(errorResponse);
    assertThat(ex.getMessage()).isEqualTo("non-technical error");
    assertThat(ex.getErrorResponse()).isSameAs(errorResponse);
  }

  @Test
  void sessionExpiredExceptionIsSubtypeOfTechnicalException()
  {
    SessionExpiredException ex = new SessionExpiredException("session expired");
    assertThat(ex).isInstanceOf(TechnicalException.class);
    assertThat(ex.getMessage()).isEqualTo("session expired");
  }

  @Test
  void sessionExpiredExceptionWithCause()
  {
    Throwable cause = new RuntimeException("auth failure");
    SessionExpiredException ex = new SessionExpiredException("expired", cause);
    assertThat(ex.getCause()).isSameAs(cause);
  }

  @Test
  void sessionExpiredExceptionFromThrowable()
  {
    Throwable cause = new RuntimeException("auth error");
    SessionExpiredException ex = new SessionExpiredException(cause);
    assertThat(ex.getMessage()).isEqualTo("Session expired");
    assertThat(ex.getCause()).isSameAs(cause);
  }
}
