package obs.util.api;

import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.CompletedFileUpload;
import obs.util.model.ActiveVideo;
import obs.util.model.Video;
import obs.util.service.VideosService;

import java.util.List;

import static io.micronaut.http.HttpStatus.CREATED;
import static io.micronaut.http.MediaType.MULTIPART_FORM_DATA;

@Controller(VideoController.URI)
public class VideoController {
  public static final String URI = "/v1/videos";
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
  public Video create(CompletedFileUpload video) throws Exception {
    byte[] bytes = video.getBytes();
    return videosService.add(bytes);
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

  @Get("/_active/resource/next")
  public void activeNextResource() {
    videosService.nextResource();
  }

  @Get("/_active/resource/prev")
  public void activePrevResource() {
    videosService.prevResource();
  }

  @Get("/_active/resource/start")
  public void activeStartResource() {
    videosService.startResource();
  }
}
