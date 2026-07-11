package de.ruu.lib.archunit;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestFieldWithAccessors
{
	private static class ClassWithFluentAccessorsOnly
	{
		private boolean booleanField;

		public boolean                  booleanField()               { return booleanField;        }
		public ClassWithFluentAccessorsOnly booleanField(boolean val) { booleanField = val; return this; }
	}

	@Test void testFieldsAndAccessors()
	{
		JavaClass clazz = new ClassFileImporter().importClass(ClassWithFluentAccessorsOnly.class);

		FieldWithAccessors fieldAndAccessors =
				new FieldWithAccessors(clazz.getAllFields().iterator().next());

		// For fluent style accessors, the getter and setter should be present
		assertThat(fieldAndAccessors.getter().isPresent()).isEqualTo(true);
		assertThat(fieldAndAccessors.setter().isPresent()).isEqualTo(true);
	}
}
