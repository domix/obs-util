package obs.util;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Builder
public class FileProps {
  private String cron;
  private String destination;
  private String format;

  @Tolerate
  public FileProps() {
    log.trace("Creating FleProps");
  }
}
