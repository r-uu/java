package de.ruu.app.pragma.dto;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class GroupDto
{
    private @Nullable Long id;
    private @Nullable Short version;
    private @NotBlank String name;
    private @Nullable String description;
    private boolean active = true;

    protected GroupDto()
    {
        name = "";
    }

    public GroupDto(String name)
    {
        this.name = requireNonNull(name, "name");
    }

    public @Nullable Long id() { return id; }
    public @Nullable Short version() { return version; }
    public String name() { return name; }
    public Optional<String> description() { return ofNullable(description); }
    public boolean active() { return active; }

    public GroupDto id(@Nullable Long value) { id = value; return this; }
    public GroupDto version(@Nullable Short value) { version = value; return this; }
    public GroupDto name(String value) { name = requireNonNull(value, "name"); return this; }
    public GroupDto description(@Nullable String value) { description = value; return this; }
    public GroupDto active(boolean value) { active = value; return this; }
}
