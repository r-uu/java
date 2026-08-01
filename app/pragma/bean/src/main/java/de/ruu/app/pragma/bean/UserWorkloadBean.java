package de.ruu.app.pragma.bean;

public record UserWorkloadBean(
    Long userId,
    String username,
    String displayName,
    double capacityHoursPerDay,
    double assignedHours,
    double overbookedHours
)
{
}
