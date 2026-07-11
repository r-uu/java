package de.ruu.lib.fx.control.textfield.number;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

/** Validates {@link BigDecimal} against {@link #predicate} and, if present, runs {@link #postValidateAction}. */
public class BigDecimalValidation implements BiConsumer<TextField, BigDecimal>
{
	@NonNull
	private Predicate<BigDecimal> predicate;
	private Optional<TextFieldPostValidateAction> postValidateAction;

	public BigDecimalValidation(@NonNull Predicate<BigDecimal> predicate)
	{
		this.predicate           = Objects.requireNonNull(predicate, "predicate");
		this.postValidateAction  = Optional.empty();
	}

	public BigDecimalValidation(Predicate<BigDecimal> predicate, TextFieldPostValidateAction postValidateAction)
	{
		this(predicate);
		this.postValidateAction = Optional.ofNullable(postValidateAction);
	}

	public Predicate<BigDecimal> predicate() { return predicate; }

	@Override public void accept(TextField textField, BigDecimal integer)
	{
		boolean valid = predicate.test(integer);

		if (postValidateAction.isPresent())
		{
			postValidateAction.get().accept(textField, valid);
		}
	}
}
