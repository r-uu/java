package de.ruu.app.pragma.fx.test;

import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class FxTestHarness
{
  private static final AtomicBoolean STARTED = new AtomicBoolean(false);

  private FxTestHarness() { }

  public static void startFx()
  {
    if (STARTED.get()) return;
    if (STARTED.compareAndSet(false, true))
    {
      CountDownLatch latch = new CountDownLatch(1);
      Platform.startup(latch::countDown);
      await(latch);
    }
  }

  public static void runOnFxThread(Runnable action)
  {
    if (Platform.isFxApplicationThread())
    {
      action.run();
      return;
    }
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(() -> {
      try { action.run(); }
      finally { latch.countDown(); }
    });
    await(latch);
  }

  private static void await(CountDownLatch latch)
  {
    try
    {
      if (!latch.await(10, TimeUnit.SECONDS))
        throw new IllegalStateException("Timeout while waiting for JavaFX thread.");
    }
    catch (InterruptedException e)
    {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while waiting for JavaFX thread.", e);
    }
  }
}
