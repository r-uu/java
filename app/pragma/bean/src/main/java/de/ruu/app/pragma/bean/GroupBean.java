package de.ruu.app.pragma.bean;

import org.jspecify.annotations.Nullable;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class GroupBean
{
    private @Nullable Long id;
    private @Nullable Short version;
    private String name;
    private @Nullable String description;
    private boolean active = true;

    public GroupBean(String name)
    {
        this.name = requireNonNull(name, "name");
    }

    public GroupBean(GroupBean in)
    {
        this.id = in.id;
        this.version = in.version;
        this.name = in.name;
        this.description = in.description;
        this.active = in.active;
    }

    public @Nullable Long id() { return id; }
    public @Nullable Short version() { return version; }
    public String name() { return name; }
    public Optional<String> description() { return ofNullable(description); }
    public boolean active() { return active; }

    public GroupBean id(@Nullable Long value) { id = value; return this; }
    public GroupBean version(@Nullable Short value) { version = value; return this; }
    public GroupBean name(String value) { name = requireNonNull(value, "name"); return this; }
    public GroupBean description(@Nullable String value) { description = value; return this; }
    public GroupBean active(boolean value) { active = value; return this; }
}
