package de.ruu.lib.fx.comp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Event that can be thrown to indicate that a {@link FXCApp} has started successfully. */
public class FXComponentReadyEvent extends FXComponentReadyEventAbstract<FXCView<FXCService>, FXCService>
{
	private static final Logger log = LoggerFactory.getLogger(FXComponentReadyEvent.class);

	public FXComponentReadyEvent(final FXCView<FXCService> view, final FXCService service) {
		super(view, service);
	}

	// public FXCView<FXCService> view() { return source(); }
	//
	// public FXCService service() { return data().orElseThrow(() -> new IllegalStateException("data must not be
	// absent"));}

	/** programmatically specify command line vm option {@code --add-reads de.ruu.lib.fx.comp=ALL-UNNAMED} */
	public static void addReadsUnnamedModule()
	{
		FXComponentReadyEvent.class.getModule().addReads(FXComponentReadyEvent.class.getClassLoader().getUnnamedModule());
	}
}