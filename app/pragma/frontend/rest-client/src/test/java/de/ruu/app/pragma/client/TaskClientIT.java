package de.ruu.app.pragma.client;

import de.ruu.app.pragma.bean.TaskBean;
import de.ruu.app.pragma.bean.TaskGroupBean;
import de.ruu.lib.junit.DisabledOnServerNotListening;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisabledOnServerNotListening(propertyNameHost = "pragma.rest-api.host", propertyNamePort = "pragma.rest-api.port")
class TaskClientIT
{
    private TaskGroupClient groupClient;
    private TaskClient      taskClient;
    private TaskGroupBean   testGroup;

    @BeforeEach
    void setUp()
    {
        groupClient = new TaskGroupClient();
        groupClient.postConstruct();
        taskClient = new TaskClient();
        taskClient.postConstruct();

        testGroup = groupClient.create(new TaskGroupBean("it-task-group-" + System.currentTimeMillis()));
    }

    @AfterEach
    void tearDown()
    {
        if (testGroup != null && testGroup.id() != null)
            groupClient.delete(testGroup);
        taskClient.preDestroy();
        groupClient.preDestroy();
    }

    @Test
    void testFindAllByGroup()
    {
        List<TaskBean> tasks = taskClient.findAll(testGroup);
        assertThat(tasks).isNotNull();
    }

    @Test
    void testCreateAndDelete()
    {
        String name = "it-task-" + System.currentTimeMillis();
        TaskBean created = taskClient.create(new TaskBean(testGroup, name));

        assertThat(created).isNotNull();
        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo(name);

        taskClient.delete(created);

        Optional<TaskBean> found = taskClient.findById(created.id());
        assertThat(found).isEmpty();
    }

    @Test
    void testUpdate()
    {
        TaskBean created = taskClient.create(new TaskBean(testGroup, "it-update-orig-" + System.currentTimeMillis()));
        assertThat(created.id()).isNotNull();

        created.name("it-update-new-" + System.currentTimeMillis());
        TaskBean updated = taskClient.update(created);

        assertThat(updated.name()).isEqualTo(created.name());

        taskClient.delete(created);
    }

    @Test
    void testUpdateWithStaleVersionBean()
    {
        TaskBean created = taskClient.create(new TaskBean(testGroup, "it-stale-orig-" + System.currentTimeMillis()));
        assertThat(created.id()).isNotNull();

        TaskBean firstEditor = taskClient.findByIdWithRelated(created.id()).orElseThrow();
        TaskBean secondEditor = taskClient.findByIdWithRelated(created.id()).orElseThrow();

        firstEditor.name("it-stale-first-" + System.currentTimeMillis());
        TaskBean firstSaved = taskClient.update(firstEditor);
        assertThat(firstSaved.name()).startsWith("it-stale-first-");

        secondEditor.name("it-stale-second-" + System.currentTimeMillis());
        TaskBean secondSaved = taskClient.update(secondEditor);
        assertThat(secondSaved.name()).startsWith("it-stale-second-");

        TaskBean latest = taskClient.findById(created.id()).orElseThrow();
        assertThat(latest.name()).isEqualTo(secondSaved.name());

        taskClient.delete(latest);
    }

    @Test
    void testCreateSubTask()
    {
        String parentName = "it-parent-task-" + System.currentTimeMillis();
        TaskBean parentTask = taskClient.create(new TaskBean(testGroup, parentName));
        assertThat(parentTask.id()).isNotNull();

        String subTaskName = "it-sub-task-" + System.currentTimeMillis();
        TaskBean subTask = new TaskBean(testGroup, subTaskName);
        subTask.parentTask(parentTask);
        TaskBean createdSubTask = taskClient.create(subTask);

        assertThat(createdSubTask.id()).isNotNull();
        assertThat(createdSubTask.parentTask()).isPresent();
        assertThat(createdSubTask.parentTask().get().id()).isEqualTo(parentTask.id());

        taskClient.delete(createdSubTask);
        taskClient.delete(parentTask);
    }

    @Test
    void testFindById()
    {
        TaskBean created = taskClient.create(new TaskBean(testGroup, "it-findbyid-" + System.currentTimeMillis()));
        assertThat(created.id()).isNotNull();

        Optional<TaskBean> found = taskClient.findById(created.id());
        assertThat(found).isPresent();
        assertThat(found.get().name()).isEqualTo(created.name());

        taskClient.delete(created);
    }
}
