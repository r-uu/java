package de.ruu.app.pragma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class AuthAccountDto
{
    private @Nullable Long id;
    private @Nullable Short version;
    private @NotNull Long userId;
    private @NotBlank String passwordHash;
    private boolean loginEnabled = true;
    private @Nullable LocalDateTime lastLoginAt;

    protected AuthAccountDto()
    {
        userId = 0L;
        passwordHash = "";
    }

    public AuthAccountDto(Long userId, String passwordHash)
    {
        this.userId = requireNonNull(userId, "userId");
        this.passwordHash = requireNonNull(passwordHash, "passwordHash");
    }

    public @Nullable Long id() { return id; }
    public @Nullable Short version() { return version; }
    public Long userId() { return userId; }
    public String passwordHash() { return passwordHash; }
    public boolean loginEnabled() { return loginEnabled; }
    public Optional<LocalDateTime> lastLoginAt() { return ofNullable(lastLoginAt); }

    public AuthAccountDto id(@Nullable Long value) { id = value; return this; }
    public AuthAccountDto version(@Nullable Short value) { version = value; return this; }
    public AuthAccountDto userId(Long value) { userId = requireNonNull(value, "userId"); return this; }
    public AuthAccountDto passwordHash(String value) { passwordHash = requireNonNull(value, "passwordHash"); return this; }
    public AuthAccountDto loginEnabled(boolean value) { loginEnabled = value; return this; }
    public AuthAccountDto lastLoginAt(@Nullable LocalDateTime value) { lastLoginAt = value; return this; }
}
