package obs.util.model;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum ResourceType {
  NEWS_ITEM("https://cloudnative.mx/media/icons/news.png", "Noticia", 0),
  TWEET("https://cloudnative.mx/media/icons/tweet.png", "Tweet", 1),
  REFERENCE("https://cloudnative.mx/media/icons/resource.png", "Referencia", 2),
  SOURCE_CODE_REPO("https://cloudnative.mx/media/icons/source-code.png", "Repo chingón de código", 3),
  EVENT("https://cloudnative.mx/media/icons/event.png", "Evento", 4);

  private final String iconUrl;
  private final String name;
  private final Integer order;

  ResourceType(String iconUrl, String name, Integer order) {
    this.iconUrl = iconUrl;
    this.name = name;
    this.order = order;
  }
}
