package de.ruu.app.pragma.client.dbcommand;

import de.ruu.app.pragma.bean.TaskBean;
import de.ruu.app.pragma.bean.TaskGroupBean;
import de.ruu.app.pragma.client.TaskClient;
import de.ruu.app.pragma.client.TaskGroupClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.Optional;

public class DBPopulate
{
    private static final Logger log = LogManager.getLogger(DBPopulate.class);

    public static void main(String[] args)
    {
        TaskGroupClient groupClient = new TaskGroupClient();
        groupClient.postConstruct();

        TaskClient      taskClient  = new TaskClient();
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
        log.info("Populating database ...");
        populateDatabase(groupClient, taskClient);
        log.info("Done.");
    }

    private static void populateDatabase(TaskGroupClient groupClient, TaskClient taskClient)
    {
        int         featureSetCount = 10;
        int tasksPerFeatureSetCount =  4;

        TaskGroupBean project = groupClient.create(new TaskGroupBean("project pragma"));

        LocalDate offset = LocalDate.of(2026, 1, 1);

        for (int i = 0; i < featureSetCount; i++)
        {
            // create feature set (root task)
            log.info("creating feature set {}", i + 1);
            TaskBean featureSet =
                taskClient.create
                (
                    new TaskBean(project, "feature set " + (i + 1))
                        .scheduledStart (offset)
                        .scheduledFinish(offset.plusDays(i + 30))
                );

            Optional<TaskBean> mostRecentFeature = Optional.empty();

            for (int j = 0; j < tasksPerFeatureSetCount; j++)
            {
                log.info("creating feature set {}.{}", i + 1,  j + 1);
                String suffix = switch (j)
                {
                    case 0 -> " - analyse";
                    case 1 -> " - design";
                    case 2 -> " - implement";
                    case 3 -> " - test";
                    default -> "";
                };

                // create feature (child task)
                TaskBean feature =
                    taskClient.create
                        (
                            new TaskBean(project, "feature " + (i + 1) + "." + (j + 1) + suffix)
                                .parentTask(featureSet)
                                .scheduledStart(offset .plusDays(j))
                                .scheduledFinish(offset.plusDays(j + 5))
                        );

                if (mostRecentFeature.isPresent())
                {
                    taskClient.addPredecessor(feature, mostRecentFeature.get());
                }

                mostRecentFeature = Optional.of(feature);
            }

            offset = offset.plusDays(7);
        }

//        TaskBean featureSet1 = taskClient.create(new TaskBean(project, "feature set 1")
//                .scheduledStart(LocalDate.of(2025, 1,  1))
//                .scheduledFinish(LocalDate.of(2025, 1, 31)));
//        TaskBean featureSet2 = taskClient.create(new TaskBean(project, "feature set 2")
//                .scheduledStart(LocalDate.of(2025, 1,  8))
//                .scheduledFinish(LocalDate.of(2025, 2,  7)));
//        TaskBean featureSet3 = taskClient.create(new TaskBean(project, "feature set 3")
//                .scheduledStart(LocalDate.of(2025, 1, 17))
//                .scheduledFinish(LocalDate.of(2025, 2, 15)));
//
//        TaskBean f11 = taskClient.create(new TaskBean(project, "feature 1.1 - analyse"  ).scheduledStart(LocalDate.of(2025, 1,  1)).scheduledFinish(LocalDate.of(2025, 1,  7)));
//        TaskBean f12 = taskClient.create(new TaskBean(project, "feature 1.2 - design"   ).scheduledStart(LocalDate.of(2025, 1,  8)).scheduledFinish(LocalDate.of(2025, 1, 15)));
//        TaskBean f13 = taskClient.create(new TaskBean(project, "feature 1.3 - implement").scheduledStart(LocalDate.of(2025, 1, 16)).scheduledFinish(LocalDate.of(2025, 1, 25)));
//        TaskBean f14 = taskClient.create(new TaskBean(project, "feature 1.4 - test"     ).scheduledStart(LocalDate.of(2025, 1, 26)).scheduledFinish(LocalDate.of(2025, 1, 31)));
//        TaskBean f21 = taskClient.create(new TaskBean(project, "feature 2.1 - analyse"  ).scheduledStart(LocalDate.of(2025, 1,  8)).scheduledFinish(LocalDate.of(2025, 1, 15)));
//        TaskBean f22 = taskClient.create(new TaskBean(project, "feature 2.2 - design"   ).scheduledStart(LocalDate.of(2025, 1, 16)).scheduledFinish(LocalDate.of(2025, 1, 25)));
//        TaskBean f23 = taskClient.create(new TaskBean(project, "feature 2.3 - implement").scheduledStart(LocalDate.of(2025, 1, 26)).scheduledFinish(LocalDate.of(2025, 1, 31)));
//        TaskBean f24 = taskClient.create(new TaskBean(project, "feature 2.4 - test"     ).scheduledStart(LocalDate.of(2025, 2,  1)).scheduledFinish(LocalDate.of(2025, 2,  7)));
//        TaskBean f31 = taskClient.create(new TaskBean(project, "feature 3.1 - analyse"  ).scheduledStart(LocalDate.of(2025, 1, 17)).scheduledFinish(LocalDate.of(2025, 1, 25)));
//        TaskBean f32 = taskClient.create(new TaskBean(project, "feature 3.2 - design"   ).scheduledStart(LocalDate.of(2025, 1, 26)).scheduledFinish(LocalDate.of(2025, 1, 31)));
//        TaskBean f33 = taskClient.create(new TaskBean(project, "feature 3.3 - implement").scheduledStart(LocalDate.of(2025, 2,  1)).scheduledFinish(LocalDate.of(2025, 2,  7)));
//        TaskBean f34 = taskClient.create(new TaskBean(project, "feature 3.4 - test"     ).scheduledStart(LocalDate.of(2025, 2,  8)).scheduledFinish(LocalDate.of(2025, 2, 15)));
//
//        taskClient.setParentTask(f11, featureSet1);
//        taskClient.setParentTask(f12, featureSet1);
//        taskClient.setParentTask(f13, featureSet1);
//        taskClient.setParentTask(f14, featureSet1);
//        taskClient.setParentTask(f21, featureSet2);
//        taskClient.setParentTask(f22, featureSet2);
//        taskClient.setParentTask(f23, featureSet2);
//        taskClient.setParentTask(f24, featureSet2);
//        taskClient.setParentTask(f31, featureSet3);
//        taskClient.setParentTask(f32, featureSet3);
//        taskClient.setParentTask(f33, featureSet3);
//        taskClient.setParentTask(f34, featureSet3);
//
//        taskClient.addPredecessor(f12, f11);
//        taskClient.addPredecessor(f13, f12);
//        taskClient.addPredecessor(f14, f13);
//        taskClient.addPredecessor(f22, f21);
//        taskClient.addPredecessor(f23, f22);
//        taskClient.addPredecessor(f24, f23);
//        taskClient.addPredecessor(f32, f31);
//        taskClient.addPredecessor(f33, f32);
//        taskClient.addPredecessor(f34, f33);
//        taskClient.addPredecessor(featureSet2, featureSet1);
//        taskClient.addPredecessor(featureSet3, featureSet2);
//        taskClient.addPredecessor(f21, f11); // predecessor outside parent/child hierarchy
    }
}
