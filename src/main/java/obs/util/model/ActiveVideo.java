package obs.util.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class ActiveVideo {
  private Video video = null;
  private Integer resourceIndex = 0;

  public void incResourceIndex() {
    log.info("Index before inc: {}", resourceIndex);
    resourceIndex++;
    log.info("Index after inc: {}", resourceIndex);
  }

  public void decResourceIndex() {
    log.info("Index before dec: {}", resourceIndex);
    resourceIndex--;
    log.info("Index after dec: {}", resourceIndex);
  }

  public void resetResourceIndex() {
    log.info("Index before reset: {}", resourceIndex);
    resourceIndex = 0;
    log.info("Index after reset: {}", resourceIndex);
  }
}
