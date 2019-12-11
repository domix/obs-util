package obs.util.model;

import lombok.Builder;
import lombok.Getter;

import java.util.concurrent.ScheduledFuture;

@Builder
@Getter
public class MyTask {
  private ScheduledFuture<FileProps> scheduledFuture;
  private FileProps fileProps;
}
