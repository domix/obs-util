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
  private List<Resource> resources;
}
