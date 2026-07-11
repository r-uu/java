package de.ruu.lib.fx.css;

public enum FontWeight
{
	  NORMAL
	, BOLD
	, BOLDER
	, LIGHTER
	, _100
	, _200
	, _300
	, _400
	, _500
	, _600
	, _700
	, _800
	, _900
	;

	private FontWeight() { this.value = name().replaceFirst("_", "").toLowerCase(); }

	public final String value;

	public static void main(String[] args) { System.out.println(_100.value); }
}
