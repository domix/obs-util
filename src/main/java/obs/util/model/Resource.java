package obs.util.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static obs.util.model.ResourceType.NEWS_ITEM;

@Getter
@Setter
@ToString
public class Resource {
  private String name;
  private String url;
  private ResourceType type = NEWS_ITEM;
}
