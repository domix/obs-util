package obs.util.service;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.server.event.ServerShutdownEvent;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Slf4j
@Singleton
public class CleanFilesService implements ApplicationEventListener<ServerShutdownEvent> {
  private final DateJob dateJob;

  public CleanFilesService(DateJob dateJob) {
    this.dateJob = dateJob;
  }

  @Override
  public void onApplicationEvent(ServerShutdownEvent event) {
    log.info("Cleaning all files...");
    dateJob.allTasks().forEach(props -> {
      dateJob.removeTask(props.getId());
    });
  }

  @Override
  public boolean supports(ServerShutdownEvent event) {
    return true;
  }
}
