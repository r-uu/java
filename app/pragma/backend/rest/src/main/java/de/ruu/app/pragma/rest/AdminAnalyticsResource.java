package de.ruu.app.pragma.rest;

import de.ruu.app.pragma.core.AvailabilityType;
import de.ruu.app.pragma.dto.TaskOverrunDto;
import de.ruu.app.pragma.dto.UserWorkloadDto;
import de.ruu.app.pragma.jpa.TaskAssignmentJPA;
import de.ruu.app.pragma.jpa.TaskJPA;
import de.ruu.app.pragma.jpa.UserAvailabilityJPA;
import de.ruu.app.pragma.jpa.UserJPA;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/admin/analytics")
@RequestScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(SecurityRoles.PRAGMA_ADMIN)
public class AdminAnalyticsResource
{
    private static final double DEFAULT_CAPACITY_HOURS_PER_DAY = 8d;

    @PersistenceContext
    private EntityManager em;

    @GET
    @Path("/workload")
    public List<UserWorkloadDto> workload()
    {
        List<UserJPA> users = em.createQuery("SELECT u FROM UserJPA u WHERE u.active = true ORDER BY u.username", UserJPA.class).getResultList();
        List<TaskAssignmentJPA> assignments = em.createQuery(
            "SELECT a FROM TaskAssignmentJPA a WHERE a.active = true AND a.user IS NOT NULL", TaskAssignmentJPA.class).getResultList();
        List<UserAvailabilityJPA> availabilities = em.createQuery(
            "SELECT a FROM UserAvailabilityJPA a ORDER BY a.fromDate DESC", UserAvailabilityJPA.class).getResultList();
        Map<Long, Double> assignedHoursByUser = new HashMap<>();
        for (TaskAssignmentJPA assignment : assignments) {
            Long userId = assignment.user().map(UserJPA::id).orElse(null);
            if (userId == null) continue;
            Double taskHours = assignment.task().workEstimateCurrent().orElse(assignment.task().workEstimateInitial().orElse(0d));
            double share = assignment.share().orElse(1d);
            assignedHoursByUser.merge(userId, (taskHours == null ? 0d : taskHours) * share, Double::sum);
        }
        Map<Long, Double> latestCapacityByUser = new HashMap<>();
        for (UserAvailabilityJPA availability : availabilities) {
            Long userId = availability.user().id();
            if (userId == null || latestCapacityByUser.containsKey(userId)) continue;
            latestCapacityByUser.put(userId, availability.availabilityType() == AvailabilityType.ABSENT ? 0d : availability.capacityHoursPerDay());
        }
        return users.stream().map(user -> {
            double capacity = latestCapacityByUser.getOrDefault(user.id(), DEFAULT_CAPACITY_HOURS_PER_DAY);
            double assigned = assignedHoursByUser.getOrDefault(user.id(), 0d);
            return Mappings.toDtoUserWorkload(user, capacity, assigned);
        }).toList();
    }

    @GET
    @Path("/time-overruns")
    public List<TaskOverrunDto> timeOverruns()
    {
        List<TaskJPA> tasks = em.createQuery("SELECT t FROM TaskJPA t ORDER BY t.id", TaskJPA.class).getResultList();
        return tasks.stream()
            .map(Mappings::toDtoTaskOverrun)
            .filter(it -> it.overrunHours() != null && it.overrunHours() > 0d)
            .sorted((a, b) -> Double.compare(b.overrunHours(), a.overrunHours()))
            .toList();
    }
}
