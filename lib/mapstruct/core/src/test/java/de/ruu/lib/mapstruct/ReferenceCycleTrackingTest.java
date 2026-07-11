package de.ruu.lib.mapstruct;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReferenceCycleTrackingTest
{
  private ReferenceCycleTracking tracker;

  @BeforeEach
  void setup()
  {
    tracker = new ReferenceCycleTracking();
  }

  @Test
  void getReturnsNullForUnknownSource()
  {
    Object source = new Object();
    String result = tracker.get(source, String.class);
    assertThat(result).isNull();
  }

  @Test
  void putAndGetRoundTrip()
  {
    Object source = new Object();
    String target = "mapped";
    tracker.put(source, target);

    String result = tracker.get(source, String.class);
    assertThat(result).isSameAs(target);
  }

  @Test
  void mapReflectsStoredMappings()
  {
    Object source = new Object();
    String target = "target";
    tracker.put(source, target);

    assertThat(tracker.map()).containsEntry(source, target);
  }

  @Test
  void mapIsImmutable()
  {
    assertThatThrownBy(() -> tracker.map().put(new Object(), new Object()))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void multipleSourcesTrackedIndependently()
  {
    Object source1 = new Object();
    Object source2 = new Object();
    String target1 = "t1";
    String target2 = "t2";

    tracker.put(source1, target1);
    tracker.put(source2, target2);

    assertThat(tracker.get(source1, String.class)).isSameAs(target1);
    assertThat(tracker.get(source2, String.class)).isSameAs(target2);
  }

  @Test
  void putRequiresNonNullSource()
  {
    assertThatThrownBy(() -> tracker.put(null, "target"))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void putRequiresNonNullTarget()
  {
    assertThatThrownBy(() -> tracker.put(new Object(), null))
        .isInstanceOf(NullPointerException.class);
  }
}
