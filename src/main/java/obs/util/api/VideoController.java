package obs.util.api;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;
import obs.util.model.Video;
import obs.util.service.VideosService;

import java.io.IOException;
import java.util.List;

import static io.micronaut.http.MediaType.MULTIPART_FORM_DATA;

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

  @Post(consumes = MULTIPART_FORM_DATA)
  public String create(CompletedFileUpload video) {
    try {

      byte[] bytes = video.getBytes();
      videosService.foo(bytes);
      return video.getFilename() + ": " + bytes.length;
    } catch (Exception e) {
      return e.getMessage();
    }
  }
}
