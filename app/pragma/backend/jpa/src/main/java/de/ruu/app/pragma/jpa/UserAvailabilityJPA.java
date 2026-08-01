package de.ruu.app.pragma.jpa;

import de.ruu.app.pragma.core.AvailabilityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@Entity
@Table(name = "user_availability")
public class UserAvailabilityJPA
{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_availability_seq")
    @SequenceGenerator(name = "user_availability_seq", sequenceName = "user_availability_seq", allocationSize = 50)
    private @Nullable Long id;

    @Version
    private @Nullable Short version;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJPA user;

    @Column(nullable = false)
    private LocalDate fromDate;

    @Column(nullable = false)
    private LocalDate toDate;

    @Column(nullable = false)
    private double capacityHoursPerDay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AvailabilityType availabilityType = AvailabilityType.AVAILABLE;

    @Column(length = 4000)
    private @Nullable String note;

    protected UserAvailabilityJPA()
    {
        user = new UserJPA();
        fromDate = LocalDate.now();
        toDate = fromDate;
    }

    public UserAvailabilityJPA(UserJPA user, LocalDate fromDate, LocalDate toDate, double capacityHoursPerDay)
    {
        this.user = requireNonNull(user, "user");
        this.fromDate = requireNonNull(fromDate, "fromDate");
        this.toDate = requireNonNull(toDate, "toDate");
        this.capacityHoursPerDay = capacityHoursPerDay;
    }

    public @Nullable Long id() { return id; }
    public @Nullable Short version() { return version; }
    public UserJPA user() { return user; }
    public LocalDate fromDate() { return fromDate; }
    public LocalDate toDate() { return toDate; }
    public double capacityHoursPerDay() { return capacityHoursPerDay; }
    public AvailabilityType availabilityType() { return availabilityType; }
    public Optional<String> note() { return ofNullable(note); }

    public UserAvailabilityJPA user(UserJPA value) { user = requireNonNull(value, "user"); return this; }
    public UserAvailabilityJPA fromDate(LocalDate value) { fromDate = requireNonNull(value, "fromDate"); return this; }
    public UserAvailabilityJPA toDate(LocalDate value) { toDate = requireNonNull(value, "toDate"); return this; }
    public UserAvailabilityJPA capacityHoursPerDay(double value) { capacityHoursPerDay = value; return this; }
    public UserAvailabilityJPA availabilityType(AvailabilityType value) { availabilityType = requireNonNull(value, "availabilityType"); return this; }
    public UserAvailabilityJPA note(@Nullable String value) { note = value; return this; }
}
