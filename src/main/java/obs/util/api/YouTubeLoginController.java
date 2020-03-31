package obs.util.api;

import com.google.api.client.auth.oauth2.Credential;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import lombok.SneakyThrows;
import obs.util.service.YouTubeService;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

@Controller("/v1/youtube")
public class YouTubeLoginController {

  private final YouTubeService service;

  public YouTubeLoginController(YouTubeService service) {
    this.service = service;
  }

  @SneakyThrows
  @Post("/{username}")
  public String login(String username) {

    Credential authorize = service.authorize(username);

    return authorize.getAccessToken();
  }

  @Get("/{username}/videos")
  public List<Map> getVideos(String username) {
    return service.videosDataFor(username);
  }

  @Get("/{username}/streams")
  public List<Map> getStreams(String username) {
    service.foo(username);
    return emptyList();
  }

  @Get("/{username}/broadcasts")
  public List<Map> getBroadcasts(String username) {
    service.broadcast(username);
    return emptyList();
  }

  @Get("/{username}/broadcasts/messages/{chatId}")
  public List<Map> getBroadcasts(String username, String chatId) {
    service.liveChatMessages(username, chatId);
    return emptyList();
  }
}
