package obs.util.service;

import lombok.extern.slf4j.Slf4j;
import obs.util.model.Video;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Singleton
public class VideosService {
  private ConcurrentMap<String, Video> storage;

  public VideosService() {
    storage = new ConcurrentHashMap<>();
  }

  public List<Video> allTasks() {
    return new ArrayList<>(storage.values());
  }
}
