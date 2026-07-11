package de.ruu.lib.fx.control.textfield.number;

import java.util.function.BiConsumer;

import javafx.scene.control.TextField;

/**
 * {@link FunctionalInterface} for {@link TextField}s that are configured to execute a validation after each change
 * of {@link TextField#textProperty()}. Instances are provided with the {@link TextField} and a boolean result of the
 * validation.
 * <p>
 * For configuration of {@link TextField}s with a validator see {@link IntegerTextFieldValueChangeListener}.
 */
@FunctionalInterface
public interface TextFieldPostValidateAction extends BiConsumer<TextField, Boolean> { }