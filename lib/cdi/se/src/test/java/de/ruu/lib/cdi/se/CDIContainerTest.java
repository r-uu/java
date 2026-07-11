package de.ruu.lib.cdi.se;

import jakarta.enterprise.inject.spi.CDI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class CDIContainerTest
{
  @BeforeAll
  static void setup()
  {
    CDIContainer.bootstrap();
  }

  @Test
  void containerIsRunning()
  {
    assertThatCode(() -> CDI.current()).doesNotThrowAnyException();
  }

  @Test
  void bootstrapIsIdempotent()
  {
    assertThatCode(() -> CDIContainer.bootstrap()).doesNotThrowAnyException();
  }

  @Test
  void beanCanBeSelected()
  {
    TestBean bean = CDI.current().select(TestBean.class).get();
    assertThat(bean).isNotNull();
    assertThat(bean.hello()).isEqualTo("hello");
  }
}
