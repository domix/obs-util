package obs.util.api;

import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.CompletedFileUpload;
import obs.util.model.ActiveVideo;
import obs.util.model.Video;
import obs.util.service.VideosService;

import java.util.List;

import static io.micronaut.http.HttpStatus.CREATED;
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

  @Post
  @Status(CREATED)
  @Consumes(MULTIPART_FORM_DATA)
  public String create(CompletedFileUpload video) {
    try {
      byte[] bytes = video.getBytes();
      videosService.add(bytes);
      return video.getFilename() + ": " + bytes.length;
    } catch (Exception e) {
      return e.getMessage();
    }
  }

  @Put("/{id}")
  public Video setActive(String id) {
    return videosService.setVideoActive(id);
  }

  @Get("/_active")
  public ActiveVideo active() {
    return videosService.getActive();
  }

  @Delete("/_active")
  public void inactive() {
    videosService.inactive();
  }
}
