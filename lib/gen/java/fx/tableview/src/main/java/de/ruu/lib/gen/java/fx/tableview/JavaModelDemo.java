package de.ruu.lib.gen.java.fx.tableview;

import java.math.BigDecimal;
import java.util.List;

import de.ruu.lib.gen.java.fx.bean.FXProperty;

public interface JavaModelDemo
{
	@FXProperty boolean      aBoolean();
	@FXProperty int          anInteger();
	@FXProperty String       aString();
	@FXProperty BigDecimal   aBigDecimal();
	@FXProperty List<String> stringList();
}