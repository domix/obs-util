package obs.util;

import io.micronaut.scheduling.TaskScheduler;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

@Singleton
public class DateJob {

  private final TaskScheduler taskScheduler;

  public DateJob(TaskScheduler taskScheduler) {
    this.taskScheduler = taskScheduler;
  }

  //@Scheduled(cron = "* * * * * *")
  //@Scheduled(cron = "* 0 0 ? * * *")
  public void foo() {
    System.out.println("Creando trabajo...");
    FileProps build = FileProps.builder()
      .cron("* * * * * *")
      .destination("")
      .build();
    taskScheduler.schedule(build.getCron(), new DatetimeFileWriter(build));
  }
}
