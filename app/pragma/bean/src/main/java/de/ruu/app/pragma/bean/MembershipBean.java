package de.ruu.app.pragma.bean;

import de.ruu.app.pragma.core.MembershipRole;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class MembershipBean
{
    private @Nullable Long id;
    private @Nullable Short version;
    private Long userId;
    private Long groupId;
    private MembershipRole roleInGroup = MembershipRole.MEMBER;
    private @Nullable LocalDate validFrom;
    private @Nullable LocalDate validTo;
    private boolean active = true;

    public MembershipBean(Long userId, Long groupId)
    {
        this.userId = requireNonNull(userId, "userId");
        this.groupId = requireNonNull(groupId, "groupId");
    }

    public MembershipBean(MembershipBean in)
    {
        this.id = in.id;
        this.version = in.version;
        this.userId = in.userId;
        this.groupId = in.groupId;
        this.roleInGroup = in.roleInGroup;
        this.validFrom = in.validFrom;
        this.validTo = in.validTo;
        this.active = in.active;
    }

    public @Nullable Long id() { return id; }
    public @Nullable Short version() { return version; }
    public Long userId() { return userId; }
    public Long groupId() { return groupId; }
    public MembershipRole roleInGroup() { return roleInGroup; }
    public Optional<LocalDate> validFrom() { return ofNullable(validFrom); }
    public Optional<LocalDate> validTo() { return ofNullable(validTo); }
    public boolean active() { return active; }

    public MembershipBean id(@Nullable Long value) { id = value; return this; }
    public MembershipBean version(@Nullable Short value) { version = value; return this; }
    public MembershipBean userId(Long value) { userId = requireNonNull(value, "userId"); return this; }
    public MembershipBean groupId(Long value) { groupId = requireNonNull(value, "groupId"); return this; }
    public MembershipBean roleInGroup(MembershipRole value) { roleInGroup = requireNonNull(value, "roleInGroup"); return this; }
    public MembershipBean validFrom(@Nullable LocalDate value) { validFrom = value; return this; }
    public MembershipBean validTo(@Nullable LocalDate value) { validTo = value; return this; }
    public MembershipBean active(boolean value) { active = value; return this; }
}
