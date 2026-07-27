package de.ruu.lib.fx.comp;

import de.ruu.lib.cdi.se.EventDispatcher;
import de.ruu.lib.util.AbstractEvent;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** Event that can be thrown to indicate that a {@link FXCApp} has started successfully. */
public class FXCAppStartedEvent extends AbstractEvent<FXCApp, FXCView<? extends FXCService>>
{
	private static final Logger log = LogManager.getLogger(FXCAppStartedEvent.class);

	@ApplicationScoped
	public static class FXCAppStartedEventDispatcher extends EventDispatcher<FXCAppStartedEvent>
	{
	}

	public FXCAppStartedEvent(final FXCApp source, final FXCView<? extends FXCService> data) {
		super(source, data);
	}

	/** programmatically specify command line vm option {@code --add-reads de.ruu.lib.fx.comp=ALL-UNNAMED} */
	public static void addReadsUnnamedModule()
	{
		FXCAppStartedEvent.class.getModule().addReads(FXCAppStartedEvent.class.getClassLoader().getUnnamedModule());
	}
}