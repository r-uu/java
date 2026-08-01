package de.ruu.app.pragma.dto;

public record TaskOverrunDto(
    Long taskId,
    String taskName,
    Double estimateHours,
    Double actualHours,
    Double overrunHours
)
{
}
