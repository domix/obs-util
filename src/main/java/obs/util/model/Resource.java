package obs.util.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static obs.util.model.VideoResource.NEWS_ITEM;

@Getter
@Setter
@ToString
public class Resource {
  private String name;
  private String url;
  private VideoResource type = NEWS_ITEM;
}
