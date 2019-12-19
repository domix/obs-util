package obs.util.api;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import obs.util.model.Video;
import obs.util.service.VideosService;

import java.util.List;

@Controller("/v1/videos")
public class VideoController {
  private final VideosService videosService;

  public VideoController(VideosService videosService) {
    this.videosService = videosService;
  }

  @Get
  public List<Video> list() {
    return videosService.allTasks();
  }
}
