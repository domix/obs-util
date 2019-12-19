package obs.util.model;

public enum VideoResource {
  NEWS_ITEM("https://cloudnative.mx/media/icons/news.png", "Noticia"),
  TWEET("https://cloudnative.mx/media/icons/event.png", "Tweet"),
  REFERENCE("https://cloudnative.mx/media/icons/resource.png", "Referencia"),
  SOURCE_CODE_REPO("https://cloudnative.mx/media/icons/source-code.png", "Repo chingón de código"),
  EVENT("https://cloudnative.mx/media/icons/event.png", "Evento");

  private final String iconUrl;
  private final String name;

  VideoResource(String iconUrl, String name) {
    this.iconUrl = iconUrl;
    this.name = name;
  }
}
