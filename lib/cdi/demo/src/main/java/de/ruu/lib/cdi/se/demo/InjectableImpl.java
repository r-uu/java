package de.ruu.lib.cdi.se.demo;

import jakarta.inject.Singleton;

@Singleton
public class InjectableImpl implements Injectable
{
	@Override public String ping() { return "ping"; }
}
