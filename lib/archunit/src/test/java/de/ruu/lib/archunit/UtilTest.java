package de.ruu.lib.archunit;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link Util}.
 *
 * <p>These tests verify the utility methods that operate on ArchUnit domain classes
 * such as {@link JavaClass}, {@link JavaType}, and {@link JavaMethod}.</p>
 *
 * <p>We use simple Java classes (e.g. {@link String}, {@link Integer}, and collections)
 * as input to check the behavior.</p>
 */
class UtilTest
{
		private final ClassFileImporter importer = new ClassFileImporter();

    // --- isPublic ------------------------------------------------------------

		@Test void isPublic_shouldReturnTrueForPublicMethod()
		{
        JavaClass  javaClass = importer.importClass(Sample.class);
        JavaMethod method    = javaClass.getMethod("publicMethod");

        assertThat(Util.isPublic(method)).isEqualTo(true);
    }

    @Test void isPublic_shouldReturnFalseForPrivateMethod()
    {
        JavaClass  javaClass = importer.importClass(Sample.class);
        JavaMethod method    = javaClass.getMethod("privateMethod");

        assertThat(Util.isPublic(method)).isEqualTo(false);
    }

    // --- fieldsAndAccessors --------------------------------------------------

    @Test void fieldsAndAccessors_shouldReturnAllFields()
    {
			List<FieldWithAccessors> fields = Util.fieldsWithAccessors(Sample.class);

			assertThat(fields).isNotEmpty();
			assertThat(tryToExtractFrom(fields, "stringField").isPresent()).isEqualTo(true);
			assertThat(tryToExtractFrom(fields, "intField"   ).isPresent()).isEqualTo(true);
    }

    // --- type checks ---------------------------------------------------------

    @Test void isCollection_shouldDetectCollections()
    {
        JavaClass listClass = importer.importClass(List.class);
        assertThat(Util.isCollection(listClass)).isEqualTo(true);
    }

    @Test void isGeneric_shouldDetectParameterizedTypes()
    {
        JavaClass listClass = importer.importClass(List.class);
        assertThat(Util.isGeneric(listClass)).isEqualTo(true);
    }

    @Test void isPrimitive_shouldDetectPrimitiveTypes()
    {
	    List<FieldWithAccessors> fields = Util.fieldsWithAccessors(Sample.class);

	    assertThat(fields).isNotEmpty();

	    Optional<FieldWithAccessors> optional =
			    fields
              .stream()
              .filter(field -> "intField".equals(field.javaField().getName()))
              .findFirst();

	    assertThat(optional.isPresent()).isEqualTo(true);

	    FieldWithAccessors fieldWithAccessors = optional.get();
			assertThat(Util.isPrimitive(fieldWithAccessors.javaField().getType())).isEqualTo(true);
    }

    @Test void isNumeric_shouldDetectIntegerAsNumeric()
    {
        JavaClass integerClass = importer.importClass(Integer.class);
        assertThat(Util.isNumeric(integerClass)).isEqualTo(true);
    }

    @Test void isNumeric_shouldReturnFalseForNonNumericClass()
    {
        JavaClass stringClass = importer.importClass(String.class);
        assertThat(Util.isNumeric(stringClass)).isEqualTo(false);
    }

    // --- generic type arguments ----------------------------------------------

    @Test void actualTypeArguments_shouldReturnTypeArgumentsForParameterizedType()
    {
        JavaType type = importer.importClass(SampleGeneric.class).getField("list").getType();

        Optional<List<JavaType>> args = Util.actualTypeArguments(type);

        assertThat(args.isPresent()).isEqualTo(true);
        assertThat(args.get()).hasSize(1);
    }

    @Test void firstActualTypeArgument_shouldReturnFirstArgument()
    {
        JavaType type = importer.importClass(SampleGeneric.class).getField("list").getType();

        Optional<JavaType> arg = Util.firstActualTypeArgument(type);

        assertThat(arg.isPresent()).isEqualTo(true);
    }

    @Test void isParameterisedType_shouldDetectParameterizedType()
    {
        JavaType type = importer.importClass(SampleGeneric.class).getField("list").getType();

        assertThat(Util.isParameterisedType(type)).isEqualTo(true);
    }

    // --- publicMethodsWithAnnotationAndSortedByName --------------------------

    @Test void publicMethodsWithAnnotationAndSortedByName_shouldReturnMethodsSortedByName()
    {
        JavaClass        javaClass = importer.importClass(Sample.class);
        List<JavaMethod> methods   = Util.publicMethodsWithAnnotationAndSortedByName(javaClass, Deprecated.class);

        // collect method names to check order
        List<String> methodNames =
		        methods
				        .stream()
                .map(JavaMethod::getName)
                .collect(Collectors.toList());

        // ensure list is sorted alphabetically
        List<String> sortedNames =
		        methodNames
				        .stream()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        assertThat(methodNames).isEqualTo(sortedNames);
    }

	private Optional<FieldWithAccessors> tryToExtractFrom(List<FieldWithAccessors> fields, String name)
	{
		return fields.stream().filter(field -> name.equals(field.javaField().getName())).findFirst();
	}

    // --- helper classes for testing ------------------------------------------

    static class Sample
    {
      public String stringField;
	    public int    intField;

        public  void publicMethod () {}
        private void privateMethod() {}
        public  void anotherMethod() {}
    }

    static class SampleGeneric
    {
        public List<String> list;
    }
}
