package de.ruu.lib.ws.rs;

import org.junit.jupiter.api.Test;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest
{
  @Test
  void singleArgConstructorSetsMessage()
  {
    ErrorResponse response = new ErrorResponse("test message");
    assertThat(response.message()).isEqualTo("test message");
    assertThat(response.cause()).isEqualTo("");
    assertThat(response.httpStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
  }

  @Test
  void twoArgConstructorSetsCause()
  {
    ErrorResponse response = new ErrorResponse("msg", "some cause");
    assertThat(response.message()).isEqualTo("msg");
    assertThat(response.cause()).isEqualTo("some cause");
    assertThat(response.httpStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
  }

  @Test
  void threeArgConstructorSetsAllFields()
  {
    ErrorResponse response = new ErrorResponse("msg", "cause", NOT_FOUND);
    assertThat(response.message()).isEqualTo("msg");
    assertThat(response.cause()).isEqualTo("cause");
    assertThat(response.httpStatus()).isEqualTo(NOT_FOUND);
  }

  @Test
  void defaultStatusIsInternalServerError()
  {
    ErrorResponse response = new ErrorResponse("error");
    assertThat(response.httpStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
  }
}
