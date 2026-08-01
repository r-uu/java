package de.ruu.app.pragma.jpa;

import de.ruu.app.pragma.core.MembershipRole;
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
@Table(name = "membership")
public class MembershipJPA
{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "membership_seq")
    @SequenceGenerator(name = "membership_seq", sequenceName = "membership_seq", allocationSize = 50)
    private @Nullable Long id;

    @Version
    private @Nullable Short version;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJPA user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupJPA group;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private MembershipRole roleInGroup = MembershipRole.MEMBER;

    @Column
    private @Nullable LocalDate validFrom;

    @Column
    private @Nullable LocalDate validTo;

    @Column(nullable = false)
    private boolean active = true;

    protected MembershipJPA()
    {
        user = new UserJPA();
        group = new GroupJPA();
    }

    public MembershipJPA(UserJPA user, GroupJPA group)
    {
        this.user = requireNonNull(user, "user");
        this.group = requireNonNull(group, "group");
    }

    public @Nullable Long id() { return id; }
    public @Nullable Short version() { return version; }
    public UserJPA user() { return user; }
    public GroupJPA group() { return group; }
    public MembershipRole roleInGroup() { return roleInGroup; }
    public Optional<LocalDate> validFrom() { return ofNullable(validFrom); }
    public Optional<LocalDate> validTo() { return ofNullable(validTo); }
    public boolean active() { return active; }

    public MembershipJPA user(UserJPA value) { user = requireNonNull(value, "user"); return this; }
    public MembershipJPA group(GroupJPA value) { group = requireNonNull(value, "group"); return this; }
    public MembershipJPA roleInGroup(MembershipRole value) { roleInGroup = requireNonNull(value, "roleInGroup"); return this; }
    public MembershipJPA validFrom(@Nullable LocalDate value) { validFrom = value; return this; }
    public MembershipJPA validTo(@Nullable LocalDate value) { validTo = value; return this; }
    public MembershipJPA active(boolean value) { active = value; return this; }
}
