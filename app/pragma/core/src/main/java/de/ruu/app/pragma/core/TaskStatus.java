package de.ruu.app.pragma.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskStatus
{
    // Wire names are lower-case because they are shown directly in API payloads and the UI.
    NEW("new"),
    IN_PROGRESS("in progress"),
    CLOSED("closed"),
    ON_HOLD("on hold"),
    REJECTED("rejected");

    private final String wireName;

    TaskStatus(String wireName)
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
    public static TaskStatus from(String value)
    {
        if (value == null) throw new IllegalArgumentException("status must not be null");
        String normalized = value.trim().toLowerCase().replace('_', ' ');
        for (TaskStatus status : values())
            if (status.wireName.equals(normalized) || status.name().toLowerCase().replace('_', ' ').equals(normalized))
                return status;
        throw new IllegalArgumentException("Unknown task status: " + value);
    }
}
