package de.ruu.app.pragma.bean;

import de.ruu.app.pragma.core.AvailabilityType;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class UserAvailabilityBean
{
    private @Nullable Long id;
    private @Nullable Short version;
    private Long userId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private double capacityHoursPerDay;
    private AvailabilityType availabilityType = AvailabilityType.AVAILABLE;
    private @Nullable String note;

    public UserAvailabilityBean(Long userId, LocalDate fromDate, LocalDate toDate, double capacityHoursPerDay)
    {
        this.userId = requireNonNull(userId, "userId");
        this.fromDate = requireNonNull(fromDate, "fromDate");
        this.toDate = requireNonNull(toDate, "toDate");
        this.capacityHoursPerDay = capacityHoursPerDay;
    }

    public UserAvailabilityBean(UserAvailabilityBean in)
    {
        this.id = in.id;
        this.version = in.version;
        this.userId = in.userId;
        this.fromDate = in.fromDate;
        this.toDate = in.toDate;
        this.capacityHoursPerDay = in.capacityHoursPerDay;
        this.availabilityType = in.availabilityType;
        this.note = in.note;
    }

    public @Nullable Long id() { return id; }
    public @Nullable Short version() { return version; }
    public Long userId() { return userId; }
    public LocalDate fromDate() { return fromDate; }
    public LocalDate toDate() { return toDate; }
    public double capacityHoursPerDay() { return capacityHoursPerDay; }
    public AvailabilityType availabilityType() { return availabilityType; }
    public Optional<String> note() { return ofNullable(note); }

    public UserAvailabilityBean id(@Nullable Long value) { id = value; return this; }
    public UserAvailabilityBean version(@Nullable Short value) { version = value; return this; }
    public UserAvailabilityBean userId(Long value) { userId = requireNonNull(value, "userId"); return this; }
    public UserAvailabilityBean fromDate(LocalDate value) { fromDate = requireNonNull(value, "fromDate"); return this; }
    public UserAvailabilityBean toDate(LocalDate value) { toDate = requireNonNull(value, "toDate"); return this; }
    public UserAvailabilityBean capacityHoursPerDay(double value) { capacityHoursPerDay = value; return this; }
    public UserAvailabilityBean availabilityType(AvailabilityType value) { availabilityType = requireNonNull(value, "availabilityType"); return this; }
    public UserAvailabilityBean note(@Nullable String value) { note = value; return this; }
}
