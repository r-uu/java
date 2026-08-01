package de.ruu.app.pragma.dto;

import de.ruu.app.pragma.core.MembershipRole;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class MembershipDto
{
    private @Nullable Long id;
    private @Nullable Short version;
    private @NotNull Long userId;
    private @NotNull Long groupId;
    private MembershipRole roleInGroup = MembershipRole.MEMBER;
    private @Nullable LocalDate validFrom;
    private @Nullable LocalDate validTo;
    private boolean active = true;

    protected MembershipDto()
    {
        userId = 0L;
        groupId = 0L;
    }

    public MembershipDto(Long userId, Long groupId)
    {
        this.userId = requireNonNull(userId, "userId");
        this.groupId = requireNonNull(groupId, "groupId");
    }

    public @Nullable Long id() { return id; }
    public @Nullable Short version() { return version; }
    public Long userId() { return userId; }
    public Long groupId() { return groupId; }
    public MembershipRole roleInGroup() { return roleInGroup; }
    public Optional<LocalDate> validFrom() { return ofNullable(validFrom); }
    public Optional<LocalDate> validTo() { return ofNullable(validTo); }
    public boolean active() { return active; }

    public MembershipDto id(@Nullable Long value) { id = value; return this; }
    public MembershipDto version(@Nullable Short value) { version = value; return this; }
    public MembershipDto userId(Long value) { userId = requireNonNull(value, "userId"); return this; }
    public MembershipDto groupId(Long value) { groupId = requireNonNull(value, "groupId"); return this; }
    public MembershipDto roleInGroup(MembershipRole value) { roleInGroup = requireNonNull(value, "roleInGroup"); return this; }
    public MembershipDto validFrom(@Nullable LocalDate value) { validFrom = value; return this; }
    public MembershipDto validTo(@Nullable LocalDate value) { validTo = value; return this; }
    public MembershipDto active(boolean value) { active = value; return this; }
}
