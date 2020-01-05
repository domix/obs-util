package obs.util.service;

import io.micronaut.context.annotation.Value;
import io.reactivex.Maybe;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import obs.util.model.ActiveVideo;
import obs.util.model.Participant;
import obs.util.model.Resource;
import obs.util.model.Video;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
  @Getter
  private String baseDirectory;

  public VideosService(DateJob dateJob, @Value("${basedir:~/.obs-util}") String baseDir) throws IOException {
    if (baseDir.startsWith("~/")) {
      String replace = baseDir.replace("~/", "");
      String home = System.getProperty("user.home");
      this.baseDirectory = home + "/" + replace;
    } else {
      this.baseDirectory = baseDir;
    }

    this.dateJob = dateJob;
    yaml = new Yaml();

    log.info("Using base dir '{}'", baseDirectory);
    Path path = Paths.get(baseDirectory);
    Files.createDirectories(path);
    activeVideo.setBaseWorkDir(this.baseDirectory);
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
        log.info("Activating video {}...", video.getId());
        setActiveVideo(video, 0);

        writeActiveVideoInfo(video, false);

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
    //TODO: reset files also
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
    if (Objects.nonNull(video)) {
      var index = activeVideo.getResourceIndex();
      var resource = video.getResources().get(index);

      dateJob.writeToFile(activeVideo.getActiveResourceTitleFile(), resource.getName());
      dateJob.writeToFile(activeVideo.getActiveResourceUrlFile(), resource.getUrl());
      dateJob.writeToFile(activeVideo.getActiveResourceDescriptionFile(), resource.getDescription());
      dateJob.writeToFile(activeVideo.getActiveResourceSummaryFile(), resource.getSummary());
      dateJob.writeToFile(activeVideo.getActiveResourceTypeIconFile(), resource.getType().getIconUrl());
      dateJob.writeToFile(activeVideo.getActiveResourceTypeNameFile(), resource.getType().getName());

      return resource;
    }
    //TODO: improve this
    return null;
  }

  public Maybe<Resource> nextResource() {
    return resource(ActiveVideo::incResourceIndex, null);
  }

  public Maybe<Resource> prevResource() {
    return resource(ActiveVideo::decResourceIndex, null);
  }

  public Maybe<Resource> startResource() {
    return resource(ActiveVideo::resetResourceIndex, null);
  }

  public void writeActiveVideoInfo(Boolean clean) throws IOException {
    Video video = activeVideo.getVideo();
    if (Objects.nonNull(video)) {
      writeActiveVideoInfo(video, clean);
    }
  }

  public void writeActiveVideoInfo(Video video, Boolean clean) throws IOException {
    log.info("About to write general files...");

    String showName = clean ? "" : video.getShowName();
    String showTitle = clean ? "" : video.getShowTitle();
    String showSubtitle = clean ? "" : video.getShowSubtitle();

    dateJob.writeToFile(activeVideo.getShowNameFile(), showName);
    dateJob.writeToFile(activeVideo.getShowTitleFile(), showTitle);
    dateJob.writeToFile(activeVideo.getShowSubtitleFile(), showSubtitle);
    log.info("General files has been written.");

    log.info("About to write Participant files...");
    for (int i = 0; i < video.getParticipants().size(); i++) {
      try {
        Participant participant = video.getParticipants().get(i);
        String roleName = clean ? "" : participant.getRole().getName();
        String name = clean ? "" : participant.getName();
        String twitter = clean ? "" : participant.getTwitter();
        String github = clean ? "" : participant.getGithub();
        String company = clean ? "" : participant.getCompany();
        String companyTitle = clean ? "" : participant.getCompanyTitle();

        dateJob.writeToFile(activeVideo.getParticipantRoleFile(i), roleName);
        dateJob.writeToFile(activeVideo.getParticipantNameFile(i), name);
        dateJob.writeToFile(activeVideo.getParticipantTwitterFile(i), twitter);
        dateJob.writeToFile(activeVideo.getParticipantGitHubFile(i), github);
        dateJob.writeToFile(activeVideo.getParticipantCompanyFile(i), company);
        dateJob.writeToFile(activeVideo.getParticipantCompanyTitleFile(i), companyTitle);
      } catch (Throwable t) {
        log.error(t.getMessage(), t);
      }

    }
  }
}
