package de.ruu.lib.jsonb;

import jakarta.json.bind.Jsonb;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import static de.ruu.lib.util.StringBuilders.rTrimChars;
import static de.ruu.lib.util.StringBuilders.sb;
import static org.assertj.core.api.Assertions.assertThat;
@Disabled("there is a subtle problem with the jsonb configurator that causes the test to fail in github actions")
class TestJsonBWithMaxEncapsulationForData
{
	private static final Logger log = LoggerFactory.getLogger(TestJsonBWithMaxEncapsulationForData.class);

	private final static Type SET_OF_PARENTS = new HashSet<Parent>()
			{
				@Serial private static final long serialVersionUID = 5432832750500023858L;
			}.getClass().getGenericSuperclass();
	private final static int NUMBER_OF_PARENTS  = 3;
	private final static int NUMBER_OF_CHILDREN = 3;

	@Test void toJson()
	{
		log.debug("""
				parents with children as json:
				{}""",
				getContext().toJson(createTestData()));
	}

	@Test void fromJson()
	{
		Set<Parent> parentsWithChildrenIn = createTestData();
		Jsonb jsonb = getContext();
		log.debug("""
				{}
				attempting to serialise set of parents
				{}""",
				"-".repeat(80),
				"-".repeat(80));
		String json = jsonb.toJson(parentsWithChildrenIn);
		log.debug("""
				{}
				returned from serialise set of parents:
				{}
				{}""",
				"-".repeat(80),
				json,
				"-".repeat(80));
		Set<Parent> parentsWithChildrenOut = jsonb.fromJson(json, SET_OF_PARENTS);
		StringBuilder sb = sb("\nparents with children\n");
		parentsWithChildrenOut.forEach(p -> sb.append(p).append("\n"));
		log.debug(rTrimChars(sb, "\n").toString());

		assertThat(parentsWithChildrenOut.size())
				.as("wrong number of parents")
				.isEqualTo(NUMBER_OF_PARENTS);

		parentsWithChildrenOut.forEach
		(
				parent ->
				{
					assertThat(parent.getChildren()).isNotNull();
					assertThat(parent.getField()).isNotNull();

					assertThat(parent.getChildren().size())
							.as("wrong number of children")
							.isEqualTo(NUMBER_OF_CHILDREN);
					parent.getChildren().forEach(child -> assertThat(child.getField()).isNotNull());
				}
		);
	}

	private Jsonb getContext() { return JsonbConfigurator.context(); }

	private Set<Parent> createTestData()
	{
		Set<Parent> result = new HashSet<>();
		for (int i = 0; i < NUMBER_OF_PARENTS; i++)
		{
			Parent parent = new Parent("" + i);
			for (int j = 0; j < NUMBER_OF_CHILDREN; j++)
			{
				parent.getChildren().add(new Child("c." + j));
			}
			result.add(parent);
		}
		return result;
	}
}
