package obs.util.api;

import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.multipart.PartData;
import io.reactivex.Maybe;
import lombok.extern.slf4j.Slf4j;
import obs.util.model.ActiveVideo;
import obs.util.model.Resource;
import obs.util.model.Video;
import obs.util.service.VideosService;

import java.io.IOException;
import java.util.List;

import static io.micronaut.http.HttpStatus.CREATED;
import static io.micronaut.http.MediaType.MULTIPART_FORM_DATA;
import static io.reactivex.Maybe.just;

@Slf4j
@Controller(VideoController.BASE_URI_VIDEOS)
public class VideoController {
  public static final String BASE_URI_VIDEOS = "/v1/videos";
  public static final String ACTIVE_VIDEO_URI = "/_active";
  public static final String ACTIVE_VIDEO_INFO_URI = ACTIVE_VIDEO_URI + "/info";

  public static final String ACTIVE_VIDEO_NEXT_RESOURCE_URI = ACTIVE_VIDEO_URI + "/resource/next";
  public static final String ACTIVE_VIDEO_PREV_RESOURCE_URI = ACTIVE_VIDEO_URI + "/resource/prev";
  public static final String ACTIVE_VIDEO_START_RESOURCE_URI = ACTIVE_VIDEO_URI + "/resource/start";
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
  public Maybe<Video> create(CompletedFileUpload video) {
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

  @Get(ACTIVE_VIDEO_URI)
  public Maybe<ActiveVideo> active() {
    return videosService.getActive();
  }

  @Delete(ACTIVE_VIDEO_URI)
  public void inactive() {
    videosService.inactive();
  }

  @Get(ACTIVE_VIDEO_NEXT_RESOURCE_URI)
  public Maybe<Resource> activeNextResource() {
    log.info("Next resource...");
    return videosService.nextResource();
  }

  @Get(ACTIVE_VIDEO_PREV_RESOURCE_URI)
  public Maybe<Resource> activePrevResource() {
    log.info("Previous resource...");
    return videosService.prevResource();
  }

  @Get(ACTIVE_VIDEO_START_RESOURCE_URI)
  public Maybe<Resource> activeStartResource() {
    log.info("Start resource...");
    return videosService.startResource();
  }

  @Get(ACTIVE_VIDEO_INFO_URI)
  public Maybe<ActiveVideo> writeInfo(@QueryValue(value = "clean", defaultValue = "false") Boolean clean) throws Exception {
    log.info("Abount to write active video info, with cleaning '{}'", clean);
    videosService.writeActiveVideoInfo(clean);
    return this.active();
  }
}
