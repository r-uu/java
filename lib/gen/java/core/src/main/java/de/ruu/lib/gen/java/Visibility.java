package de.ruu.lib.gen.java;

import javax.lang.model.element.Modifier;

public enum Visibility
{
	PUBLIC(Modifier.PUBLIC),
	PROTECTED(Modifier.PROTECTED),
	PRIVATE(Modifier.PRIVATE),
	DEFAULT(Modifier.DEFAULT),
	;

	private Modifier modifier;
	private Visibility(Modifier modifier) { this.modifier = modifier; }

	public String asString()
	{
		if (modifier == Modifier.DEFAULT) return "";
		return modifier.toString().toLowerCase();
	}

	public Modifier modifier() { return modifier; }
}