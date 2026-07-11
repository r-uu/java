package de.ruu.lib.fx.control.textfield.number;

import java.util.function.BiConsumer;

import javafx.scene.control.TextField;

/**
 * {@link FunctionalInterface} for {@link TextField}s that are configured to execute a converter call after each change
 * of {@link TextField#textProperty()}. Instances are provided with the {@link TextField} and a non {@code null} {@link
 * NumberFormatException} if the converter call raised such an exception.
 * <p>
 * For configuration of {@link TextField}s with a converter see {@link IntegerTextFieldValueChangeListener}.
 */
@FunctionalInterface
public interface NumberTextFieldPostConvertAction extends BiConsumer<TextField, NumberFormatException> { }