package de.ruu.lib.fx.comp;

import de.ruu.lib.util.AbstractEvent;

/** Event that can be thrown to indicate that a {@link FXCApp} has stopped successfully. */
public class FXCAppStoppedEvent extends AbstractEvent<FXCApp, DefaultFXCView>
{
	public FXCAppStoppedEvent(final FXCApp source) {
		super(source);
	}
}