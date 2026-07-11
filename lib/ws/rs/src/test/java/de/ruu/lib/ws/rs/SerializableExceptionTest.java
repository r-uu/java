package de.ruu.lib.ws.rs;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class SerializableExceptionTest
{
  @Test
  void capturesTypeAndMessage()
  {
    IllegalArgumentException ex = new IllegalArgumentException("bad arg");
    SerializableException se = new SerializableException(ex);

    assertThat(se.getType()).isEqualTo(IllegalArgumentException.class.getName());
    assertThat(se.getMessage()).isEqualTo("bad arg");
  }

  @Test
  void stackTraceIsCaptured()
  {
    RuntimeException ex = new RuntimeException("test");
    SerializableException se = new SerializableException(ex);

    assertThat(se.getStackTrace()).isNotEmpty();
  }

  @Test
  void causeIsCapturedRecursively()
  {
    IOException root = new IOException("root cause");
    RuntimeException wrapper = new RuntimeException("wrapper", root);
    SerializableException se = new SerializableException(wrapper);

    assertThat(se.getCause()).isNotNull();
    assertThat(se.getCause().getType()).isEqualTo(IOException.class.getName());
    assertThat(se.getCause().getMessage()).isEqualTo("root cause");
  }

  @Test
  void noCauseYieldsNullCause()
  {
    RuntimeException ex = new RuntimeException("no cause");
    SerializableException se = new SerializableException(ex);

    assertThat(se.getCause()).isNull();
  }
}
