package obs.util.service;

import lombok.extern.slf4j.Slf4j;
import obs.util.model.ActiveVideo;
import obs.util.model.Video;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Singleton
public class VideosService {
  private final ConcurrentMap<String, Video> storage;
  private final Yaml yaml;
  private ActiveVideo activeVideo = new ActiveVideo();

  public VideosService() {
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
}
