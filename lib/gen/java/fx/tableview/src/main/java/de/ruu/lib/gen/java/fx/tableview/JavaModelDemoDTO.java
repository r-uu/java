package de.ruu.lib.gen.java.fx.tableview;


import java.math.BigDecimal;
import java.util.List;

public class JavaModelDemoDTO
{
	public JavaModelDemoDTO(boolean aBoolean, int anInteger, String aString, BigDecimal aBigDecimal, java.util.List<String> stringList)
	{
		this.aBoolean   = aBoolean;
		this.anInteger  = anInteger;
		this.aString    = aString;
		this.aBigDecimal = aBigDecimal;
		this.stringList = stringList;
	}
	boolean      aBoolean;
	int          anInteger;
	String       aString;
	BigDecimal   aBigDecimal;
	List<String> stringList;
}