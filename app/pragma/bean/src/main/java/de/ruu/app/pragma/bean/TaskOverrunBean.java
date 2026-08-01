package de.ruu.app.pragma.bean;

public record TaskOverrunBean(
    Long taskId,
    String taskName,
    Double estimateHours,
    Double actualHours,
    Double overrunHours
)
{
}
