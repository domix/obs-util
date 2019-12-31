package obs.util.service;

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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

@Slf4j
@Singleton
public class VideosService {
  private final ConcurrentMap<String, Video> storage;
  private final Yaml yaml;
  private final DateJob dateJob;
  private ActiveVideo activeVideo = new ActiveVideo();

  public VideosService(DateJob dateJob) {
    this.dateJob = dateJob;
    storage = new ConcurrentHashMap<>();
    yaml = new Yaml();
  }

  public List<Video> allTasks() {
    return new ArrayList<>(storage.values());
  }

  public void add(byte[] bytes) {
    try (var is = new ByteArrayInputStream(bytes)) {
      Video video = yaml.load(is);
      log.info(video.toString());
      storage.put(video.getId(), video);
    } catch (Throwable t) {
      log.warn(t.getMessage(), t);
    }
  }

  public Video setVideoActive(String id) {
    Video video = storage.get(id);
    activeVideo.setResourceIndex(0);
    activeVideo.setVideo(video);
    return video;
  }

  public ActiveVideo getActive() {
    if (Objects.isNull(activeVideo.getVideo())) {
      return null;
    }
    return this.activeVideo;
  }

  public void inactive() {
    activeVideo.setVideo(null);
    activeVideo.setResourceIndex(0);
  }


  @SuppressWarnings("RedundantCast")
  public Optional<Resource> resource(Consumer<ActiveVideo> preAction, Consumer<ActiveVideo> postAction) {

    var activeVideo = Optional.ofNullable(getActive());
    activeVideo.map(video -> {
      
      return null;
    });
    /*return Optional.ofNullable(getActive())
      .map(activeVideo -> {
          Optional.ofNullable(preAction).ifPresent(activeVideoConsumer -> {
            activeVideoConsumer.accept(activeVideo);
          });
          var index = activeVideo.getResourceIndex();
          log.info("Index: {}", index);
          Resource resource = null;
          try {
            resource = writeResourceData(activeVideo);

            Optional.ofNullable(postAction).ifPresent(activeVideoConsumer -> {
              activeVideoConsumer.accept(activeVideo);
            });
          } catch (IOException e) {
            log.warn(e.getMessage(), e);
          }
          return Optional.ofNullable(resource);
        }
      );*/
    return null;
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

  public void nextResource() {
    resource(null, ActiveVideo::incResourceIndex);
  }

  public void prevResource() {
    resource(activeVideo1 -> {
      log.info("Ac index: {}", activeVideo1.getResourceIndex());
      activeVideo1.decResourceIndex();
    }, null);
  }

  public void startResource() {
    resource(ActiveVideo::resetResourceIndex, null);
  }
}
