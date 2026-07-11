package de.ruu.lib.fx.comp;

import de.ruu.lib.util.AbstractEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Event that can be thrown to indicate that a {@link FXCApp} has started successfully. */
public abstract class FXComponentReadyEventAbstract<V extends FXCView<S>, S extends FXCService>
		extends AbstractEvent<V, S>
{
	private static final Logger log = LoggerFactory.getLogger(FXComponentReadyEventAbstract.class);

	public FXComponentReadyEventAbstract(final V view, final S service) {
		super(view, service);
	}

	public V view()
	{
		return source();
	}

	public S service()
	{
		return data().orElseThrow(() -> new IllegalStateException("data must not be absent"));
	}

	/** programmatically specify command line vm option {@code --add-reads de.ruu.lib.fx.comp=ALL-UNNAMED} */
	public static void addReadsUnnamedModule()
	{
		FXComponentReadyEventAbstract.class.getModule()
				.addReads(FXComponentReadyEventAbstract.class.getClassLoader().getUnnamedModule());
	}
}