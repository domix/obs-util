package obs.util;

import io.micronaut.scheduling.TaskScheduler;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Singleton
public class DateJob {
  private ConcurrentMap<String, MyTask> storage;

  private final TaskScheduler taskScheduler;

  public DateJob(TaskScheduler taskScheduler) {
    this.taskScheduler = taskScheduler;
    storage = new ConcurrentHashMap<>();
  }

  //@Scheduled(cron = "* * * * * *")
  //@Scheduled(cron = "* 0 0 ? * * *")
  public void foo(FileProps build) {
    ScheduledFuture<FileProps> schedule = taskScheduler
      .schedule(build.getCronExpression(), new DatetimeFileWriter(this, build));

    MyTask myTask = MyTask.builder()
      .fileProps(build)
      .scheduledFuture(schedule)
      .build();

    storage.put(build.getId(), myTask);
  }

  public void fff(String id) {
    MyTask remove = storage.remove(id);
    remove.getScheduledFuture().cancel(false);
    Optional<FileProps> fileProps = Optional.ofNullable(remove.getFileProps());

    try {
      writeToFile(remove.getFileProps().getDestination(), "");
    } catch (Throwable t) {
      log.error(t.getMessage(), t);
    }
  }

  public void writeToFile(String destination, String str) throws IOException {
    FileOutputStream outputStream = new FileOutputStream(destination);
    byte[] strToBytes = str.getBytes();
    outputStream.write(strToBytes);
    outputStream.close();
  }
}
