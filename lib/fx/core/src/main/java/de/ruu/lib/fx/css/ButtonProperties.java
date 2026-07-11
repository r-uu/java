package de.ruu.lib.fx.css;

public enum ButtonProperties
{
	BACKGROUND_COLOR
	;

	private ButtonProperties() { this.value = "-fx-" + name().replaceAll("_", "-").toLowerCase(); }

	public final String value;

	public static void main(String[] args) { System.out.println(BACKGROUND_COLOR.value); }
}