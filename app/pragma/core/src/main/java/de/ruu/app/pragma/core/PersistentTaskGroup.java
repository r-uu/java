package de.ruu.app.pragma.core;

import de.ruu.lib.jpa.core.Entity;

public interface PersistentTaskGroup<T extends Task<?, ?>> extends TaskGroup<T>, Entity<Long>
{
}


