package de.ruu.app.pragma.client.dbcommand;

import de.ruu.app.pragma.client.TaskClient;
import de.ruu.app.pragma.client.TaskGroupClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBClear
{
    private static final Logger log = LogManager.getLogger(DBClear.class);

    public static void main(String[] args)
    {
        TaskGroupClient groupClient = new TaskGroupClient();
        TaskClient      taskClient  = new TaskClient();
        groupClient.postConstruct();
        taskClient .postConstruct();
        try
        {
            run(groupClient, taskClient);
        }
        finally
        {
            taskClient .preDestroy();
            groupClient.preDestroy();
        }
    }

    public static void run(TaskGroupClient groupClient, TaskClient taskClient)
    {
        log.info("clearing database started ...");
        // Remove all predecessor/successor relationships first to avoid FK constraint violations
        // when deleting tasks (task_predecessors_successors references tasks by FK).
        taskClient.findAll().forEach(task ->
            taskClient.findPredecessors(task).forEach(pred ->
                taskClient.removePredecessor(task, pred)));
        // Delete all groups — cascades to their tasks (and sub-task hierarchies).
        groupClient.findAll().forEach(groupClient::delete);
        log.info("clearing database finished.");
    }
}
