package de.ruu.lib.jpa.core.criteria.restriction;

/** Conjunction (AND) */
public class Conjunction<T> extends Junction<T>
{
	public Conjunction()
	{
		super("and");
	}
}