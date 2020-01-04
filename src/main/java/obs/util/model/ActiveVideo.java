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
  private Integer participantIndex = 0;

  public void incResourceIndex() {
    log.info("Resource Index before inc: {}", resourceIndex);
    resourceIndex++;
    log.info("Resource Index after inc: {}", resourceIndex);
  }

  public void decResourceIndex() {
    log.info("Resource Index before dec: {}", resourceIndex);
    resourceIndex--;
    log.info("Resource Index after dec: {}", resourceIndex);
  }

  public void resetResourceIndex() {
    log.info("Resource Index before reset: {}", resourceIndex);
    resourceIndex = 0;
    log.info("Resource Index after reset: {}", resourceIndex);
  }


  public void incParticipantIndex() {
    log.info("Participant Index before inc: {}", participantIndex);
    participantIndex++;
    log.info("Participant Index after inc: {}", participantIndex);
  }

  public void decParticipantIndex() {
    log.info("Participant Index before dec: {}", participantIndex);
    participantIndex--;
    log.info("Participant Index after dec: {}", participantIndex);
  }

  public void resetParticipantIndex() {
    log.info("Participant Index before reset: {}", participantIndex);
    participantIndex = 0;
    log.info("Participant Index after reset: {}", participantIndex);
  }
}
