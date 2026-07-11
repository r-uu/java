package de.ruu.lib.fx.control.textfield.number;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

/** Validates integer against {@link #predicate} and, if present, runs {@link #postValidateAction}. */
public class IntegerValidation implements BiConsumer<TextField, Integer>
{
	@NonNull
	private Predicate<Integer> predicate;
	private Optional<TextFieldPostValidateAction> postValidateAction;

	public IntegerValidation(@NonNull Predicate<Integer> predicate)
	{
		this.predicate          = Objects.requireNonNull(predicate, "predicate");
		this.postValidateAction = Optional.empty();
	}

	public IntegerValidation(Predicate<Integer> predicate, TextFieldPostValidateAction postValidateAction)
	{
		this(predicate);
		this.postValidateAction = Optional.ofNullable(postValidateAction);
	}

	public Predicate<Integer> predicate() { return predicate; }

	@Override public void accept(TextField textField, Integer integer)
	{
		boolean valid = predicate.test(integer);

		if (postValidateAction.isPresent())
		{
			postValidateAction.get().accept(textField, valid);
		}
	}
}
