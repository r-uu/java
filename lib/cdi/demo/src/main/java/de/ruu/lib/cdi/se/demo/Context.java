package de.ruu.lib.cdi.se.demo;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class Context
{
	@Inject private Injectable injectable;

	public Injectable injectable() { return injectable; }
}
