package de.ruu.app.pragma.bean;

import org.jspecify.annotations.Nullable;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class UserBean
{
    private @Nullable Long id;
    private @Nullable Short version;
    private String username;
    private @Nullable String keycloakUserId;
    private @Nullable String password;
    private String displayName;
    private String email;
    private boolean active = true;

    public UserBean(String username, String displayName, String email)
    {
        this.username = requireNonNull(username, "username");
        this.displayName = requireNonNull(displayName, "displayName");
        this.email = requireNonNull(email, "email");
    }

    public UserBean(UserBean in)
    {
        this.id = in.id;
        this.version = in.version;
        this.username = in.username;
        this.keycloakUserId = in.keycloakUserId;
        this.password = in.password;
        this.displayName = in.displayName;
        this.email = in.email;
        this.active = in.active;
    }

    public @Nullable Long id() { return id; }
    public @Nullable Short version() { return version; }
    public String username() { return username; }
    public Optional<String> keycloakUserId() { return Optional.ofNullable(keycloakUserId); }
    public Optional<String> password() { return Optional.ofNullable(password); }
    public String displayName() { return displayName; }
    public String email() { return email; }
    public boolean active() { return active; }

    public UserBean id(@Nullable Long value) { id = value; return this; }
    public UserBean version(@Nullable Short value) { version = value; return this; }
    public UserBean username(String value) { username = requireNonNull(value, "username"); return this; }
    public UserBean keycloakUserId(@Nullable String value) { keycloakUserId = value; return this; }
    public UserBean password(@Nullable String value) { password = value; return this; }
    public UserBean displayName(String value) { displayName = requireNonNull(value, "displayName"); return this; }
    public UserBean email(String value) { email = requireNonNull(value, "email"); return this; }
    public UserBean active(boolean value) { active = value; return this; }
}
