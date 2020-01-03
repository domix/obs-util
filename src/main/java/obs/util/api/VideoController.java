package obs.util.api;

import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.multipart.PartData;
import io.reactivex.Maybe;
import lombok.extern.slf4j.Slf4j;
import obs.util.model.ActiveVideo;
import obs.util.model.Video;
import obs.util.service.VideosService;

import java.util.List;

import static io.micronaut.http.HttpStatus.CREATED;
import static io.micronaut.http.MediaType.MULTIPART_FORM_DATA;
import static io.reactivex.Maybe.just;

@Slf4j
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
  public Maybe<Video> create(CompletedFileUpload video) throws Exception {
    return just(video)
      .map(PartData::getBytes)
      .map(videosService::add)
      .flatMap(Maybe::onErrorComplete);
  }

  @Put("/{id}")
  public Maybe<Video> setActive(String id) {
    return Maybe.just(id)
      .map(videosService::setVideoActive)
      .flatMap(Maybe::onErrorComplete);
  }

  @Get("/_active")
  public Maybe<ActiveVideo> active() {
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
