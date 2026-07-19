package de.ruu.app.pragma.core;

import de.ruu.lib.jpa.core.Entity;

public interface PersistentTask
        <G extends TaskGroup<? extends Task<G, ?>>, T extends Task<G, T>>
        extends Task<G, T>, Entity<Long>
{
}


