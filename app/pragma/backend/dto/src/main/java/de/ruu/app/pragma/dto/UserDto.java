package de.ruu.app.pragma.dto;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class UserDto
{
    private @Nullable Long id;
    private @Nullable Short version;
    private @NotBlank String username;
    private @Nullable String keycloakUserId;
    private @Nullable String password;
    private @NotBlank String displayName;
    private @NotBlank String email;
    private boolean active = true;

    protected UserDto()
    {
        username = "";
        displayName = "";
        email = "";
    }

    public UserDto(String username, String displayName, String email)
    {
        this.username = requireNonNull(username, "username");
        this.displayName = requireNonNull(displayName, "displayName");
        this.email = requireNonNull(email, "email");
    }

    public @Nullable Long id() { return id; }
    public @Nullable Short version() { return version; }
    public String username() { return username; }
    public Optional<String> keycloakUserId() { return ofNullable(keycloakUserId); }
    public Optional<String> password() { return ofNullable(password); }
    public String displayName() { return displayName; }
    public String email() { return email; }
    public boolean active() { return active; }

    public UserDto id(@Nullable Long value) { id = value; return this; }
    public UserDto version(@Nullable Short value) { version = value; return this; }
    public UserDto username(String value) { username = requireNonNull(value, "username"); return this; }
    public UserDto keycloakUserId(@Nullable String value) { keycloakUserId = value; return this; }
    public UserDto password(@Nullable String value) { password = value; return this; }
    public UserDto displayName(String value) { displayName = requireNonNull(value, "displayName"); return this; }
    public UserDto email(String value) { email = requireNonNull(value, "email"); return this; }
    public UserDto active(boolean value) { active = value; return this; }
}
