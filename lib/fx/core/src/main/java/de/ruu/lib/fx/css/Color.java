package de.ruu.lib.fx.css;

public enum Color
{
	  WHITE
	, BLACK
	;

	private Color() { this.value = name().toLowerCase(); }

	public final String value;

	public static void main(String[] args) { System.out.println(WHITE.value); }
}
