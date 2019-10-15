package obs.util;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.discovery.event.ServiceShutdownEvent;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Slf4j
@Singleton
public class CleanFilesService implements ApplicationEventListener<ServiceShutdownEvent> {
  private final DateJob dateJob;

  public CleanFilesService(DateJob dateJob) {
    this.dateJob = dateJob;
  }

  @Override
  public void onApplicationEvent(ServiceShutdownEvent event) {
    log.info("Cleaning all files...");
    dateJob.allTasks().forEach(props -> {
      dateJob.removeTask(props.getId());
    });
  }

  @Override
  public boolean supports(ServiceShutdownEvent event) {
    return true;
  }
}
