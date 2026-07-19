package de.ruu.app.pragma.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.ruu.app.pragma.core.PersistentTaskGroup;
import de.ruu.app.pragma.core.TaskGroup;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "jsonId", scope = TaskGroupDto.class)
public class TaskGroupDto implements PersistentTaskGroup<TaskDto>
{
    private final UUID jsonId  = UUID.randomUUID();

    private @Nullable Long         id;
    private @Nullable Short        version;
    private @NotBlank String       name;
    private @Nullable Set<TaskDto> tasks; // null = not yet loaded

    /** For JSON deserialization only. */
    protected TaskGroupDto() { name = ""; }

    public TaskGroupDto(String name) { this.name = requireNonNull(name, "name"); }

    /** Mapping constructor — copies persisted metadata and scalar group fields from any PersistentTaskGroup. */
    public TaskGroupDto(PersistentTaskGroup<?> in)
    {
        this.id      = in.id     ();
        this.version = in.version();
        this.name    = in.name   ();
    }

    @Override public @Nullable Long                   id     () { return            id     ; }
    @Override public @Nullable Short                  version() { return            version; }
    @Override public           String                 name   () { return            name   ; }
    @Override public           Optional<Set<TaskDto>> tasks  () { return ofNullable(tasks) ; }

    @Override public TaskGroupDto name(String n) { name = requireNonNull(n, "name"); return this; }

    @Override
    public void addTask(TaskDto task)
    {
        requireNonNull(task, "task");
        if (tasks == null) tasks = new LinkedHashSet<>();
        if (tasks.add(task)) {
            // null during construction — see HasTaskGroup javadoc
            TaskGroupDto old = task.taskGroup();
            if (old != null && old != this) old.tasks().ifPresent(t -> t.remove(task));
            task.taskGroupInternal(this);
        }
    }

    @Override
    public void removeTask(TaskDto task)
    {
        requireNonNull(task, "task");
        if (tasks != null) tasks.remove(task);
    }
}
