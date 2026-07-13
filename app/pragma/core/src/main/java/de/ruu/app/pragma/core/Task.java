package de.ruu.app.pragma.core;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Optional;

public interface Task<G extends TaskGroup<? extends Task<G, ?>>, T extends Task<G, T>>
        extends
            RawTask,
            HasMutableName,
            HasMutableParentTask<T>,
            HasMutableSubTasks<T>,
            HasMutablePredecessors<T>,
            HasMutableSuccessors<T>,
            HasMutableTaskGroup<G>
{
    Optional<String>    description        ();
    Optional<Double>    workEstimateInitial();
    Optional<Double>    workEstimateCurrent();
    Optional<Double>    workActual         ();
    /** calculated from {@link #workEstimateCurrent()} - {@link #workActual()} */
    default
    Optional<Double>    workRemaining      ()
    {
        return workEstimateCurrent().flatMap(estimate -> workActual().map(actual -> estimate - actual));
    }
    /** progress in percentage, calculated from {@link #workActual()} and {@link #workRemaining()} */
    default
    Optional<Double>    workProgress       ()
    {
        return workActual().flatMap(actual -> workRemaining().map(remaining -> (actual / (actual + remaining)) * 100));
    }
    Optional<LocalDate> scheduledStart     ();
    Optional<LocalDate> scheduledFinish    ();
    Boolean             closed             ();

    T description        (@Nullable String    description        );
    T workEstimateInitial(@Nullable Double    workEstimateInitial);
    T workEstimateCurrent(@Nullable Double    workEstimateCurrent);
    T workActual         (@Nullable Double    workActual         );
    T scheduledStart     (@Nullable LocalDate scheduledStart     );
    T scheduledFinish    (@Nullable LocalDate scheduledFinish    );
    T closed             (          Boolean   closed             );
}