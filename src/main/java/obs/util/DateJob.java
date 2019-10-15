package obs.util;

import io.micronaut.scheduling.TaskScheduler;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Slf4j
@Singleton
public class DateJob {
  private ConcurrentMap<String, MyTask> storage;

  private final TaskScheduler taskScheduler;

  public DateJob(TaskScheduler taskScheduler) {
    this.taskScheduler = taskScheduler;
    storage = new ConcurrentHashMap<>();
  }

  public FileProps schedule(FileProps build) {
    ScheduledFuture<FileProps> schedule = taskScheduler
      .schedule(build.getCronExpression(), new DatetimeFileWriter(this, build));

    MyTask myTask = MyTask.builder()
      .fileProps(build)
      .scheduledFuture(schedule)
      .build();

    storage.put(build.getId(), myTask);

    return build;
  }

  public void removeTask(String id) {
    ofNullable(storage.remove(id)).ifPresent(myTask -> {
      log.info("Removing task {}", myTask.getFileProps().getId());
      myTask.getScheduledFuture().cancel(false);
      blankFile(myTask.getFileProps().getDestination());
    });
  }

  public void blankFile(String file) {
    try {
      writeToFile(file, "");
    } catch (Throwable t) {
      log.error(t.getMessage(), t);
    }
  }

  public List<FileProps> allTasks() {
    return storage.values()
      .stream().map(MyTask::getFileProps)
      .collect(toList());
  }

  public void writeToFile(String destination, String str) throws IOException {
    FileOutputStream outputStream = new FileOutputStream(destination);
    byte[] strToBytes = str.getBytes();
    outputStream.write(strToBytes);
    outputStream.close();
  }
}
