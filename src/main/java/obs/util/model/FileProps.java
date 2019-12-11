package obs.util.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Tolerate;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Data
@Slf4j
@Builder
@ToString
public class FileProps {
  private String id;
  private String cronExpression;
  private String outputFormat;
  private String timeZone;
  private String dateFormatPattern;
  private String prefix;
  private String startedMessage;

  private String destination;
  private String startTime;

  @Tolerate
  public FileProps() {
    log.trace("Creating FleProps");
    this.id = UUID.randomUUID().toString();
    this.cronExpression = "* * * * * *";
    this.dateFormatPattern = "dd-M-yyyy hh:mm:ss a";
    this.outputFormat = "hh:mm:ss";
    this.timeZone = "America/Mexico_City";
    this.prefix = "Empezamos en ";
    this.startedMessage = "Empezando...";
  }
}
