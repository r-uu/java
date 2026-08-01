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

@Entity
@Table(name = "app_user")
public class UserJPA
{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_user_seq")
    @SequenceGenerator(name = "app_user_seq", sequenceName = "app_user_seq", allocationSize = 50)
    private @Nullable Long id;

    @Version
    private @Nullable Short version;

    @Column(nullable = false, unique = true, length = 120)
    private String username;

    @Column(unique = true, length = 120)
    private @Nullable String keycloakUserId;

    @Column(nullable = false, length = 200)
    private String displayName;

    @Column(nullable = false, length = 240)
    private String email;

    @Column(nullable = false)
    private boolean active = true;

    protected UserJPA()
    {
        username = "";
        displayName = "";
        email = "";
    }

    public UserJPA(String username, String displayName, String email)
    {
        this.username = requireNonNull(username, "username");
        this.displayName = requireNonNull(displayName, "displayName");
        this.email = requireNonNull(email, "email");
    }

    public @Nullable Long id() { return id; }
    public @Nullable Short version() { return version; }
    public String username() { return username; }
    public Optional<String> keycloakUserId() { return Optional.ofNullable(keycloakUserId); }
    public String displayName() { return displayName; }
    public String email() { return email; }
    public boolean active() { return active; }

    public UserJPA username(String value) { username = requireNonNull(value, "username"); return this; }
    public UserJPA keycloakUserId(@Nullable String value) { keycloakUserId = value; return this; }
    public UserJPA displayName(String value) { displayName = requireNonNull(value, "displayName"); return this; }
    public UserJPA email(String value) { email = requireNonNull(value, "email"); return this; }
    public UserJPA active(boolean value) { active = value; return this; }
}
