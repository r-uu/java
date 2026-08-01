package de.ruu.app.pragma.bean;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class AuthAccountBean
{
    private @Nullable Long id;
    private @Nullable Short version;
    private Long userId;
    private String passwordHash;
    private boolean loginEnabled = true;
    private @Nullable LocalDateTime lastLoginAt;

    public AuthAccountBean(Long userId, String passwordHash)
    {
        this.userId = requireNonNull(userId, "userId");
        this.passwordHash = requireNonNull(passwordHash, "passwordHash");
    }

    public AuthAccountBean(AuthAccountBean in)
    {
        this.id = in.id;
        this.version = in.version;
        this.userId = in.userId;
        this.passwordHash = in.passwordHash;
        this.loginEnabled = in.loginEnabled;
        this.lastLoginAt = in.lastLoginAt;
    }

    public @Nullable Long id() { return id; }
    public @Nullable Short version() { return version; }
    public Long userId() { return userId; }
    public String passwordHash() { return passwordHash; }
    public boolean loginEnabled() { return loginEnabled; }
    public Optional<LocalDateTime> lastLoginAt() { return ofNullable(lastLoginAt); }

    public AuthAccountBean id(@Nullable Long value) { id = value; return this; }
    public AuthAccountBean version(@Nullable Short value) { version = value; return this; }
    public AuthAccountBean userId(Long value) { userId = requireNonNull(value, "userId"); return this; }
    public AuthAccountBean passwordHash(String value) { passwordHash = requireNonNull(value, "passwordHash"); return this; }
    public AuthAccountBean loginEnabled(boolean value) { loginEnabled = value; return this; }
    public AuthAccountBean lastLoginAt(@Nullable LocalDateTime value) { lastLoginAt = value; return this; }
}
