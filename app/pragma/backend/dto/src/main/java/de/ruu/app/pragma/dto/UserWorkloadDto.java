package de.ruu.app.pragma.dto;

public record UserWorkloadDto(
    Long userId,
    String username,
    String displayName,
    double capacityHoursPerDay,
    double assignedHours,
    double overbookedHours
)
{
}
