package de.ruu.lib.jpa.core.mapstruct.demo.tree;

import jakarta.annotation.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

public interface Node<T extends Node>
{
	@NonNull String      name();
	@NonNull T           name(@NonNull String name);
	@NonNull Optional<T> parent();
	/** @throws IllegalArgumentException if {@code parent} is {@code this}, makes sure no cycles in tree */
	@NonNull T           parent(@Nullable T parent) throws IllegalArgumentException;
	/** @return unmodifiable */
	@NonNull List<T>     children();

	boolean add   (@NonNull T node);
	boolean remove(@NonNull T node);
}