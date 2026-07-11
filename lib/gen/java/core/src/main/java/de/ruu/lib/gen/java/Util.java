package de.ruu.lib.gen.java;

import static de.ruu.lib.archunit.Util.actualTypeArguments;
import static de.ruu.lib.archunit.Util.isPrimitive;
import static de.ruu.lib.util.StringBuilders.sb;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tngtech.archunit.core.domain.JavaType;

import de.ruu.lib.gen.java.naming.ImportManager;

public interface Util
{
	static StringBuilder manageImports(JavaType javaType, ImportManager importManager)
	{
		if (isPrimitive(javaType))
				return sb(javaType.getName());

		Optional<List<JavaType>> optional = actualTypeArguments(javaType);
		
		if (optional.isPresent())
		{
			StringBuilder result = sb(importManager.useType(javaType.toErasure().getName())).append("<");

			List<String> typeArguments = new ArrayList<>();

			for (JavaType javaTypeArgument : optional.get())
			{
				typeArguments.add(manageImports(javaTypeArgument, importManager).toString());
			}
			
			return result.append(String.join(", ", typeArguments)).append(">");
		}
		else
				return sb(importManager.useType(javaType.getName()));
	}
}