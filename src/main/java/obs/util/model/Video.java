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
    return baseWorkDir + "/showname.txt";
  }

  public String getShowTitleFile() {
    return baseWorkDir + "/showtitle.txt";
  }

  public String getShowSubtitleFile() {
    return baseWorkDir + "/showsubtitle.txt";
  }

  public String getActiveResourceFile() {
    return baseWorkDir + "/resourcetitle.txt";
  }
}
