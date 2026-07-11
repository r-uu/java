package de.ruu.lib.gen.java.fx.bean;

import com.tngtech.archunit.core.domain.JavaType;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface FXPropertyUtil
{
	static boolean isNumericFXPropertyType(@NonNull JavaType type)
	{
		String fullName = type.toErasure().getFullName();

		if      (fullName.equals(DoubleProperty.class.getName()))
		{
			return true;
		}
		else if (fullName.equals(FloatProperty.class.getName()))
		{
			return true;
		}
		else if (fullName.equals(IntegerProperty.class.getName()))
		{
			return true;
		}
		else if (fullName.equals(LongProperty.class.getName()))
		{
			return true;
		}

		return false;
	}

	static boolean isBooleanFXPropertyType(@NonNull JavaType type)
	{
		return type.toErasure().getFullName().equals(BooleanProperty.class.getName());
	}
}