package de.ruu.lib.mapstruct;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OptionalMapperTest
{
  private final OptionalMapper mapper = Mappers.getMapper(OptionalMapper.class);

  @Test
  void unwrapStringWithValue()
  {
    assertThat(mapper.unwrapString(Optional.of("hello"))).isEqualTo("hello");
  }

  @Test
  void unwrapStringEmpty()
  {
    assertThat(mapper.unwrapString(Optional.empty())).isNull();
  }

  @Test
  void unwrapStringNull()
  {
    assertThat(mapper.unwrapString(null)).isNull();
  }

  @Test
  void wrapStringWithValue()
  {
    assertThat(mapper.wrapString("hello")).isEqualTo(Optional.of("hello"));
  }

  @Test
  void wrapStringNull()
  {
    assertThat(mapper.wrapString(null)).isEmpty();
  }

  @Test
  void unwrapLocalDateWithValue()
  {
    LocalDate date = LocalDate.of(2024, 1, 15);
    assertThat(mapper.unwrapLocalDate(Optional.of(date))).isEqualTo(date);
  }

  @Test
  void unwrapLocalDateEmpty()
  {
    assertThat(mapper.unwrapLocalDate(Optional.empty())).isNull();
  }

  @Test
  void wrapLocalDate()
  {
    LocalDate date = LocalDate.of(2024, 6, 1);
    assertThat(mapper.wrapLocalDate(date)).isEqualTo(Optional.of(date));
  }
}
