package de.ruu.lib.archunit;

import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class TestExploreArchUnit
{
	private static final Logger log = LoggerFactory.getLogger(TestExploreArchUnit.class);

	private abstract static class AbstractClass
	{
//		private int        intFieldFromAbstract;
//		private boolean    booleanFieldFromAbstract;
//		private BigDecimal bigDecimalFromAbstract;
	}

	private static class ExtendingAbstractClass extends AbstractClass
	{
//		private int        intFieldFromExtending;
//		private boolean    booleanFieldFromExtending;
//		private BigDecimal bigDecimalFromExtending;
		private List<String> stringList;

		public List<String>           stringList()                    { return stringList;       }
		public ExtendingAbstractClass stringList(List<String> value)  { stringList = value; return this; }
	}

	private static JavaClass clazz;

	@BeforeAll static void beforeAll()
	{
		clazz = new ClassFileImporter().importClass(ExtendingAbstractClass.class);
	}

	@Test void exploreAccessesFromSelf()
	{
		Set<JavaAccess<?>> accessesFromSelf = clazz.getAccessesFromSelf();
		accessesFromSelf.forEach(a -> log.debug(a.getName()));
	}

	@Test void exploreAllAccessesFromSelf()
	{
		Set<JavaAccess<?>> accessesFromSelf = clazz.getAllAccessesFromSelf();
		accessesFromSelf.forEach(a -> log.debug(a.getName()));
	}

	@Test void exploreAllFields()
	{
		Set<JavaField> allFields = clazz.getAllFields();
		allFields.forEach(f -> log.debug(f.getType().getName() + " " + f.getName()));
		for (JavaField field : allFields)
		{
			log.debug("field {}", field);
		}
	}

	@Test void exploreAllMethods()
	{
		Set<JavaMethod> allMethods = clazz.getAllMethods();
		allMethods.forEach(m -> log.debug(m.getReturnType().getName() + " " + m.getFullName()));
	}

	@Test void exploreMethodModifiers()
	{
		Set<JavaMethod> allMethods = clazz.getAllMethods();
		allMethods.forEach(m -> log.debug(toString(m.getModifiers()) + " " + m.getFullName()));
	}

	private String toString(Set<JavaModifier> modifiers)
	{
		List<String> result = new ArrayList<>();
		modifiers.forEach(m -> result.add(m.name()));
		return String.join(",", result);
	}
}
