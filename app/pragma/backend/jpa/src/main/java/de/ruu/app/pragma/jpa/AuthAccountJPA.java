package de.ruu.app.pragma.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@Entity
@Table(name = "auth_account")
public class AuthAccountJPA
{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auth_account_seq")
    @SequenceGenerator(name = "auth_account_seq", sequenceName = "auth_account_seq", allocationSize = 50)
    private @Nullable Long id;

    @Version
    private @Nullable Short version;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserJPA user;

    @Column(nullable = false, length = 240)
    private String passwordHash;

    @Column(nullable = false)
    private boolean loginEnabled = true;

    @Column
    private @Nullable LocalDateTime lastLoginAt;

    protected AuthAccountJPA()
    {
        user = new UserJPA();
        passwordHash = "";
    }

    public AuthAccountJPA(UserJPA user, String passwordHash)
    {
        this.user = requireNonNull(user, "user");
        this.passwordHash = requireNonNull(passwordHash, "passwordHash");
    }

    public @Nullable Long id() { return id; }
    public @Nullable Short version() { return version; }
    public UserJPA user() { return user; }
    public String passwordHash() { return passwordHash; }
    public boolean loginEnabled() { return loginEnabled; }
    public Optional<LocalDateTime> lastLoginAt() { return ofNullable(lastLoginAt); }

    public AuthAccountJPA user(UserJPA value) { user = requireNonNull(value, "user"); return this; }
    public AuthAccountJPA passwordHash(String value) { passwordHash = requireNonNull(value, "passwordHash"); return this; }
    public AuthAccountJPA loginEnabled(boolean value) { loginEnabled = value; return this; }
    public AuthAccountJPA lastLoginAt(@Nullable LocalDateTime value) { lastLoginAt = value; return this; }
}
