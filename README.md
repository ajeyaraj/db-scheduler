# db-scheduler

Simple persistent scheduler for scheduled tasks, recurring or ad-hoc. The scheduler requires a single database-table.


## Examples
### Recurring tasks

Custom task class for a reoccurring task.

```java
public static class MyHourlyTask extends RecurringTask {

  public MyHourlyTask() {
    super("task_name", FixedDelay.of(Duration.ofHours(1)), createExecutionHandler());
  }

  private static ExecutionHandler createExecutionHandler() {
    return taskInstance -> {
      System.out.println("Executed!");
    };
  }
}
```

Schedule the initial execution on start-up. If an execution is already scheduled for the task, the existing execution is kept. After the execution has run, the task will be re-scheduled according to the schedule for the task (hourly in this example).

```java
private static void recurringTask(DataSource dataSource) {

  final MyHourlyTask hourlyTask = new MyHourlyTask();

  final Scheduler scheduler = Scheduler
      .create(dataSource, new SchedulerName("myscheduler"), Lists.newArrayList(hourlyTask))
      .build();

  // Schedule the task for execution now()
  scheduler.scheduleForExecution(LocalDateTime.now(), hourlyTask.instance("INSTANCE_1"));
}
```

### Ad-hoc tasks

Custom task class for an ad-hoc task.

```java
public static class MyAdhocTask extends OneTimeTask {

  public MyAdhocTask() {
    super("adhoc_task_name", createExecutionHandler());
  }

  private static ExecutionHandler createExecutionHandler() {
    return taskInstance -> {
      System.out.println("Executed!");
    };
  }
}
```

```java
private static void adhocExecution(Scheduler scheduler, MyAdhocTask myAdhocTask) {

  // Schedule the task for execution a certain time in the future
  scheduler.scheduleForExecution(LocalDateTime.now().plusMinutes(5), myAdhocTask.instance("1045"));
}
```


### Instantiating and starting the Scheduler

```java
RecurringTask recurring1 = new RecurringTask("do_something", FixedDelay.of(Duration.ofSeconds(10)), LOGGING_EXECUTION_HANDLER);
RecurringTask recurring2 = new RecurringTask("do_something_else", FixedDelay.of(Duration.ofSeconds(8)), LOGGING_EXECUTION_HANDLER);
OneTimeTask onetime = new OneTimeTask("do_something_once", LOGGING_EXECUTION_HANDLER);

final Scheduler scheduler = Scheduler
    .create(dataSource, new SchedulerName("myscheduler"), Lists.newArrayList(recurring1, recurring2, onetime))
    .build();

Runtime.getRuntime().addShutdownHook(new Thread() {
  @Override
  public void run() {
    LOG.info("Received shutdown signal.");
    scheduler.stop();
  }
});

scheduler.start();

scheduler.scheduleForExecution(now(), recurring1.instance(SINGLE_INSTANCE));
scheduler.scheduleForExecution(now(), recurring2.instance(SINGLE_INSTANCE));
scheduler.scheduleForExecution(now().plusSeconds(20), onetime.instance("1045"));

```
