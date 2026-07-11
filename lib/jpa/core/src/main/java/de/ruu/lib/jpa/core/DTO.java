package de.ruu.lib.jpa.core;

import java.io.Serializable;

public interface DTO<E extends Entity<I>, I extends Serializable> extends Serializable { }