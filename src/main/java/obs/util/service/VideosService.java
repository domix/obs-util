package obs.util.service;

import io.reactivex.Maybe;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import obs.util.model.ActiveVideo;
import obs.util.model.Resource;
import obs.util.model.Video;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import static io.reactivex.Maybe.just;
import static java.util.Optional.ofNullable;

@Slf4j
@Singleton
public class VideosService {
  private final ConcurrentMap<String, Video> storage = new ConcurrentHashMap<>();
  private final Yaml yaml;
  private final DateJob dateJob;
  private ActiveVideo activeVideo = new ActiveVideo();

  public VideosService(DateJob dateJob) {
    this.dateJob = dateJob;
    yaml = new Yaml();
  }

  private Video createVideo(byte[] bytes) {
    try (var is = new ByteArrayInputStream(bytes)) {
      Video video = yaml.load(is);
      log.info("Loaded video '{}' from bytes.", video.getId());
      return video;
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage(), t);
    }
  }

  public List<Video> allTasks() {
    return new ArrayList<>(storage.values());
  }

  public Maybe<Video> add(byte[] bytes) {
    log.info("About to add new video from bytes..");
    return just(bytes)
      .map(this::createVideo)
      .map(this::addToStorage);
  }

  public Video addToStorage(Video video) {
    storage.put(video.getId(), video);
    log.info("Video '{}' added to storage.", video.getId());
    return video;
  }

  public void resetStorage() {
    log.info("Resetting storage");
    storage.clear();
    inactive();
  }

  public Maybe<Video> findVideoById(final String id) {
    return just(id)
      .subscribeOn(Schedulers.io())
      .map(this::fromStorageOrNull);
  }

  public Video fromStorageOrNull(String id) {
    log.info("Trying to find in storage video with id: '{}'", id);
    Video video = storage.getOrDefault(id, null);
    log.info("Video found in storage? {}", Objects.nonNull(video));
    return video;
  }

  public Maybe<Video> setVideoActive(String id) {
    log.info("About to activate the video '{}'...", id);
    return Maybe.just(id)
      .map(this::findVideoById)
      .map(videoMaybe -> {
        Video video = videoMaybe.blockingGet();
        setActiveVideo(video, 0);
        log.info("Video '{}' activated.", video.getId());
        return video;
      });
  }

  public Maybe<ActiveVideo> getActive() {
    log.info("About to get ActiveVideo.");
    return Maybe.just(activeVideo);
  }

  public ActiveVideo inactive() {
    log.info("Resetting ActiveVideo");
    return setActiveVideo(null, 0);
  }

  public ActiveVideo setActiveVideo(Video video, Integer index) {
    activeVideo.setVideo(video);
    activeVideo.setResourceIndex(index);
    return activeVideo;
  }

  public Maybe<Resource> resource(Consumer<ActiveVideo> preAction, Consumer<ActiveVideo> postAction) {
    return just(activeVideo)
      .map(video -> {
        ofNullable(preAction).ifPresent(action -> action.accept(video));
        var index = video.getResourceIndex();
        log.info("Index: {}", index);

        try {
          Resource resource = writeResourceData(video);
          ofNullable(postAction).ifPresent(action -> action.accept(video));
          return just(resource);
        } catch (IOException e) {
          log.warn(e.getMessage(), e);
          throw new RuntimeException(e.getMessage(), e);
        }
      }).flatMap(Maybe::onErrorComplete);
  }

  private Resource writeResourceData(ActiveVideo activeVideo) throws IOException {
    var video = activeVideo.getVideo();
    var index = activeVideo.getResourceIndex();

    dateJob.writeToFile(video.getShowNameFile(), video.getShowName());
    dateJob.writeToFile(video.getShowTitleFile(), video.getShowTitle());
    dateJob.writeToFile(video.getShowSubtitleFile(), video.getShowSubtitle());

    var resource = video.getResources().get(index);
    dateJob.writeToFile(video.getActiveResourceFile(), resource.getName());

    return resource;
  }

  public Maybe<Resource> nextResource() {
    return resource(null, ActiveVideo::incResourceIndex);
  }

  public Maybe<Resource> prevResource() {
    return resource(activeVideo1 -> {
      log.info("Ac index: {}", activeVideo1.getResourceIndex());
      activeVideo1.decResourceIndex();
    }, null);
  }

  public Maybe<Resource> startResource() {
    return resource(ActiveVideo::resetResourceIndex, null);
  }
}
