package de.ruu.lib.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TestBidirectional
{
	private static final Logger log = LoggerFactory.getLogger(TestBidirectional.class);

    private static ObjectMapper MAPPER;

    @BeforeAll
    static void beforeAll() { MAPPER = new JacksonContextResolver().mapper(); }

    @Test void taskGroupSingle() throws JsonProcessingException
    {
        TaskGroup groupIn  = new TaskGroup("group");
        String    jsonIn   = MAPPER.writeValueAsString(groupIn);
        log.debug("jsonIn\n{}", jsonIn);

        TaskGroup groupOut = MAPPER.readValue(jsonIn, TaskGroup.class);
        String    jsonOut  = MAPPER.writeValueAsString(groupOut);
        log.debug("jsonOut\n{}", jsonOut);
        assertThat(jsonIn).as("jsonOut is not jsonIn").isEqualTo(jsonOut);

        assertThat(groupIn).as("groupOut is not groupIn").isEqualTo(groupOut);
    }

    @Test void taskGroupSingleWithTaskSingle() throws JsonProcessingException
    {
        TaskGroup groupIn = new TaskGroup("group");
        Task      taskIn  = new Task(groupIn, "task");
        String    jsonIn  = MAPPER.writeValueAsString(groupIn);
        log.debug("jsonIn\n{}", jsonIn);

        TaskGroup groupOut = MAPPER.readValue(jsonIn, TaskGroup.class);
        String    jsonOut  = MAPPER.writeValueAsString(groupOut);
        log.debug("jsonOut\n{}", jsonOut);
        assertThat(jsonIn).as("jsonOut is not jsonIn").isEqualTo(jsonOut);

        assertThat(groupIn).as("groupOut is not groupIn").isEqualTo(groupOut);
        assertThat(groupOut.tasks()).as("groupOut.tasks is null").isNotNull();
        assertThat(groupOut.tasks().size()).as("groupOut.tasks.size not 1").isEqualTo(1);

        Task taskOut = groupOut.tasks().iterator().next();

        assertThat(taskOut).as("taskOut is not taskIn").isEqualTo(taskIn);
        assertThat(taskOut.group()).as("taskOut.group is not taskIn.group").isEqualTo(taskIn.group());
    }

    @Test void taskWithAllRelationsSingle() throws JsonProcessingException
    {
        TaskGroup groupIn           = new TaskGroup(         "group");
        Task      taskIn            = new Task     (groupIn, "task");
        Task      taskSuperIn       = new Task     (groupIn, "super task");
        Task      taskSubIn         = new Task     (groupIn, "sub task");
        Task      taskPredecessorIn = new Task     (groupIn, "predecessor task");
        Task      taskSuccessorIn   = new Task     (groupIn, "successor task");

        taskIn.superTask     (taskSuperIn);
        taskIn.addSubTask    (taskSubIn);
        taskIn.addPredecessor(taskPredecessorIn);
        taskIn.addSuccessor  (taskSuccessorIn);

        String    jsonIn  = MAPPER.writeValueAsString(groupIn);
        log.debug("jsonIn\n{}", jsonIn);

        TaskGroup groupOut = MAPPER.readValue(jsonIn, TaskGroup.class);
        String    jsonOut  = MAPPER.writeValueAsString(groupOut);
        log.debug("jsonOut\n{}", jsonOut);

        assertThat(jsonIn).as("jsonOut is not jsonIn").isEqualTo(jsonOut); // sequence of elements may vary
        assertThat(groupIn).as("groupOut is not groupIn").isEqualTo(groupOut);
        assertThat(groupOut.tasks()).as("groupOut.tasks is null").isNotNull();
        assertThat(groupOut.tasks().size()).as("groupOut.tasks.size not 1").isEqualTo(5);

        Optional<Task> optional = groupOut.tasks().stream().filter(t -> t.equals(taskIn)).findFirst();
        assertThat(optional.isPresent()).as("no task out matching task in").isEqualTo(true);

        Task taskOut = optional.get();
        assertThat(taskOut).as("taskOut is not taskIn").isEqualTo(taskIn);
        assertThat(taskOut.group()).as("taskOut.group is not taskIn.group").isEqualTo(taskSuperIn.group());

        Task taskSuperOut = taskOut.superTask();
        assertThat(taskSuperOut).as("taskSuperOut is null").isNotNull();
        assertThat(taskSuperOut.group()).as("taskSuperOut.group is not taskSuperIn.group").isEqualTo(taskSuperIn.group());
        assertThat(taskSuperOut).as("taskSuperOut is taskSuperIn").isEqualTo(taskSuperIn);
        assertThat(taskSuperOut.subTasks()).as("taskSuperOut.subTasks is not null").isNotNull();
        assertThat(taskSuperOut.subTasks().size()).as("taskSuperOut.subTasks.size is not 1").isEqualTo(1);

        Task taskSubOut = taskOut.subTasks().iterator().next();
        assertThat(taskSubOut).as("taskSubOut is null").isNotNull();
        assertThat(taskSubOut.group()).as("taskSubOut.group is not taskSubIn.group").isEqualTo(taskSubIn.group());
        assertThat(taskSubOut).as("taskSubOut is taskSubIn").isEqualTo(taskSubIn);
        assertThat(taskSubOut.subTasks()).as("taskSubOut.subTasks is null").isNull();

        Task taskPredecessorOut = taskOut.predecessors().iterator().next();
        assertThat(taskPredecessorOut).as("taskPredecessorOut is null").isNotNull();
        assertThat(taskPredecessorOut.group()).as("taskPredecessorOut.group is not taskPredecessorIn.group").isEqualTo(taskPredecessorIn.group());
        assertThat(taskPredecessorOut).as("taskPredecessorOut is taskPredecessorIn").isEqualTo(taskPredecessorIn);
        assertThat(taskPredecessorOut.subTasks()).as("taskPredecessorOut.subTasks is null").isNull();
        assertThat(taskPredecessorOut.successors()).as("taskPredecessorOut.successors is not null").isNotNull();
        assertThat(taskPredecessorOut.successors().size()).as("taskPredecessorOut.successors.size is not 1").isEqualTo(1);

        Task taskSuccessorOut = taskOut.successors().iterator().next();
        assertThat(taskSuccessorOut).as("taskSuccessorOut is null").isNotNull();
        assertThat(taskSuccessorOut.group()).as("taskSuccessorOut.group is not taskSuccessorIn.group").isEqualTo(taskSuccessorOut.group());
        assertThat(taskSuccessorOut).as("taskSuccessorOut is taskSuccessorIn").isEqualTo(taskSuccessorOut);
        assertThat(taskSuccessorOut.subTasks()).as("taskSuccessorOut.subTasks is null").isNull();
        assertThat(taskSuccessorOut.predecessors()).as("taskSuccessorOut.predecessors is not null").isNotNull();
        assertThat(taskSuccessorOut.predecessors().size()).as("taskSuccessorOut.predecessors.size is not 1").isEqualTo(1);
    }
}
