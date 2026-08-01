package de.ruu.app.pragma.dto;

import de.ruu.app.pragma.core.AvailabilityType;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class UserAvailabilityDto
{
    private @Nullable Long id;
    private @Nullable Short version;
    private @NotNull Long userId;
    private @NotNull LocalDate fromDate;
    private @NotNull LocalDate toDate;
    private double capacityHoursPerDay;
    private AvailabilityType availabilityType = AvailabilityType.AVAILABLE;
    private @Nullable String note;

    protected UserAvailabilityDto()
    {
        userId = 0L;
        fromDate = LocalDate.now();
        toDate = fromDate;
    }

    public UserAvailabilityDto(Long userId, LocalDate fromDate, LocalDate toDate, double capacityHoursPerDay)
    {
        this.userId = requireNonNull(userId, "userId");
        this.fromDate = requireNonNull(fromDate, "fromDate");
        this.toDate = requireNonNull(toDate, "toDate");
        this.capacityHoursPerDay = capacityHoursPerDay;
    }

    public @Nullable Long id() { return id; }
    public @Nullable Short version() { return version; }
    public Long userId() { return userId; }
    public LocalDate fromDate() { return fromDate; }
    public LocalDate toDate() { return toDate; }
    public double capacityHoursPerDay() { return capacityHoursPerDay; }
    public AvailabilityType availabilityType() { return availabilityType; }
    public Optional<String> note() { return ofNullable(note); }

    public UserAvailabilityDto id(@Nullable Long value) { id = value; return this; }
    public UserAvailabilityDto version(@Nullable Short value) { version = value; return this; }
    public UserAvailabilityDto userId(Long value) { userId = requireNonNull(value, "userId"); return this; }
    public UserAvailabilityDto fromDate(LocalDate value) { fromDate = requireNonNull(value, "fromDate"); return this; }
    public UserAvailabilityDto toDate(LocalDate value) { toDate = requireNonNull(value, "toDate"); return this; }
    public UserAvailabilityDto capacityHoursPerDay(double value) { capacityHoursPerDay = value; return this; }
    public UserAvailabilityDto availabilityType(AvailabilityType value) { availabilityType = requireNonNull(value, "availabilityType"); return this; }
    public UserAvailabilityDto note(@Nullable String value) { note = value; return this; }
}
