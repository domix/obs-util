package obs.util.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static obs.util.model.ResourceType.NEWS_ITEM;

@Getter
@Setter
@ToString
public class Resource {
  private String name = "";
  private String url = "";
  private String description = "";
  private String summary = "";
  private ResourceType type = NEWS_ITEM;
}
