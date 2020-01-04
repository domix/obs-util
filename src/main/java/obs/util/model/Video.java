package obs.util.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class Video {
  private String id;
  private String showName;
  private String showTitle;
  private String showSubtitle;
  private String baseWorkDir;
  private List<Resource> resources;
  private List<Participant> participants;

  public String getShowNameFile() {
    return baseWorkDir + "/showName.txt";
  }

  public String getShowTitleFile() {
    return baseWorkDir + "/showTitle.txt";
  }

  public String getShowSubtitleFile() {
    return baseWorkDir + "/showSubtitle.txt";
  }

  public String getActiveResourceTitleFile() {
    return baseWorkDir + "/resourceTitle.txt";
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
}
