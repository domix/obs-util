package obs.util;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;

import static java.util.TimeZone.getTimeZone;

@Slf4j
public class DatetimeFileWriter implements Callable<FileProps> {
  public static final String ZERO = "0";
  public static final String COLON = ":";
  private final DateJob dateJob;
  private final FileProps props;

  public DatetimeFileWriter(DateJob dateJob, FileProps props) {
    this.dateJob = dateJob;
    this.props = props;
  }

  @Override
  public FileProps call() throws Exception {
    log.info("Escribiendo Archivo: {}", props.getId());
    try {
      write();
    } catch (Throwable t) {
      log.error(t.getMessage(), t);
    }
    return props;
  }

  public void write() throws Exception {
    SimpleDateFormat formatter = new SimpleDateFormat(props.getDateFormatPattern(), Locale.ENGLISH);
    formatter.setTimeZone(getTimeZone(props.getTimeZone()));

    String dateInString = props.getStartTime();
    Date date = formatter.parse(dateInString);

    Date now = new Date();
    long nowTime = now.getTime();
    long difference = date.getTime() - nowTime;
    String str;

    log.info("Now:       {}", now);
    log.info("Scheduled: {}", date);
    if (nowTime < date.getTime()) {
      StringBuilder out = new StringBuilder();
      int seconds = (int) (difference / 1000) % 60;
      int minutes = (int) ((difference / (1000 * 60)) % 60);
      int hours = (int) ((difference / (1000 * 60 * 60)) % 24);

      out.append(props.getPrefix());
      if (hours > 0) {
        out.append(hours);
        out.append(COLON);
      }
      if (minutes >= 0) {
        if (minutes < 10 && (hours > 0)) {
          out.append(ZERO);
        } else {
          if (minutes != 0) {
            out.append(minutes);
            out.append(COLON);
          }
        }
      }

      if (seconds < 10 && (hours > 0 || minutes > 0)) {
        out.append(ZERO);
      }
      if (seconds >= 0) {
        out.append(seconds);
      }
      str = out.toString();
    } else {
      str = props.getStartedMessage();
    }
    dateJob.writeToFile(props.getDestination(), str);
  }
}
