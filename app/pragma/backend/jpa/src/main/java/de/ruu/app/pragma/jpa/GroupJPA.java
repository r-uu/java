package de.ruu.app.pragma.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@Entity
@Table(name = "app_group")
public class GroupJPA
{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_group_seq")
    @SequenceGenerator(name = "app_group_seq", sequenceName = "app_group_seq", allocationSize = 50)
    private @Nullable Long id;

    @Version
    private @Nullable Short version;

    @Column(nullable = false, unique = true, length = 160)
    private String name;

    @Column(length = 4000)
    private @Nullable String description;

    @Column(nullable = false)
    private boolean active = true;

    protected GroupJPA()
    {
        name = "";
    }

    public GroupJPA(String name)
    {
        this.name = requireNonNull(name, "name");
    }

    public @Nullable Long id() { return id; }
    public @Nullable Short version() { return version; }
    public String name() { return name; }
    public Optional<String> description() { return ofNullable(description); }
    public boolean active() { return active; }

    public GroupJPA name(String value) { name = requireNonNull(value, "name"); return this; }
    public GroupJPA description(@Nullable String value) { description = value; return this; }
    public GroupJPA active(boolean value) { active = value; return this; }
}
