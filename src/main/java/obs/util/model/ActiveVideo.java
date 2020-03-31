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
  private String baseWorkDir;

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

  public String getShowNameFile() {
    return baseWorkDir + "/showName.txt";
  }

  public String getShowTitleFile() {
    return baseWorkDir + "/showTitle.txt";
  }

  public String getShowSubtitleFile() {
    return baseWorkDir + "/showSubtitle.txt";
  }

  public String getShowLogoFile() {
    return baseWorkDir + "/showLogo.png";
  }

  public String getTransparentImage() {
    return baseWorkDir + "/transparent.png";
  }

  public String getActiveCommentImage() {
    return baseWorkDir + "/activeComment.png";
  }

  public String getStartTimeFile() {
    return baseWorkDir + "/startTime.txt";
  }

  public String getActiveResourceTitleFile() {
    return baseWorkDir + "/resourceTitle.txt";
  }

  public String getMarkdownFile() {
    return baseWorkDir + "/video.md";
  }

  public String getActiveResourceUrlFile() {
    return baseWorkDir + "/resourceUrl.txt";
  }

  public String getActiveResourceDescriptionFile() {
    return baseWorkDir + "/resourceDescription.txt";
  }

  public String getActiveResourceSummaryFile() {
    return baseWorkDir + "/resourceSummary.txt";
  }

  public String getActiveResourceTypeIconFile() {
    return baseWorkDir + "/resourceTypeIcon.txt";
  }

  public String getActiveResourceTypeNameFile() {
    return baseWorkDir + "/resourceTypeName.txt";
  }

  public String getActiveResourceTypeAvatarFile() {
    return baseWorkDir + "/resourceType.png";
  }

  public String getParticipantRoleFile(Integer index) {
    return baseWorkDir + "/participant_" + index + "_role.txt";
  }

  public String getParticipantNameFile(Integer index) {
    return baseWorkDir + "/participant_" + index + "_name.txt";
  }

  public String getParticipantTwitterFile(Integer index) {
    return baseWorkDir + "/participant_" + index + "_twitter.txt";
  }

  public String getParticipantGitHubFile(Integer index) {
    return baseWorkDir + "/participant_" + index + "_github.txt";
  }

  public String getParticipantCompanyFile(Integer index) {
    return baseWorkDir + "/participant_" + index + "_company.txt";
  }

  public String getParticipantCompanyTitleFile(Integer index) {
    return baseWorkDir + "/participant_" + index + "_companyTitle.txt";
  }

  public String getParticipantAvatarFile(Integer index) {
    return baseWorkDir + "/participant_" + index + "_avatar.png";
  }

  public String getResourceTypeAvatarFile(ResourceType resourceType) {
    return baseWorkDir + "/resourceType_" + resourceType.getName() + ".png";
  }
}
