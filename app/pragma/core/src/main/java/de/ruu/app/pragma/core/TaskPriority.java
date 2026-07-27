package de.ruu.app.pragma.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskPriority
{
    // Wire names are lower-case because they are shown directly in API payloads and the UI.
    LOW("low"),
    NORMAL("normal"),
    HIGH("high"),
    IMMEDIATE("immediate");

    private final String wireName;

    TaskPriority(String wireName)
    {
        this.wireName = wireName;
    }

    @JsonValue
    @Override
    public String toString()
    {
        return wireName;
    }

    @JsonCreator
    public static TaskPriority from(String value)
    {
        if (value == null) throw new IllegalArgumentException("priority must not be null");
        String normalized = value.trim().toLowerCase().replace('_', ' ');
        for (TaskPriority priority : values())
            if (priority.wireName.equals(normalized) || priority.name().toLowerCase().replace('_', ' ').equals(normalized))
                return priority;
        throw new IllegalArgumentException("Unknown task priority: " + value);
    }
}
