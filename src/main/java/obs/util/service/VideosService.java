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
import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
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
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
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
  private String tmpFilesDirectory;

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
    tmpFilesDirectory = baseDirectory + "/.tmp";
    log.info("Using base dir '{}'", baseDirectory);

    Path baseDirectoryPath = Paths.get(baseDirectory);
    Path tmpDirectoryPath = Paths.get(tmpFilesDirectory);
    Files.createDirectories(baseDirectoryPath);
    Files.createDirectories(tmpDirectoryPath);
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
          Resource resource = writeResourceData(false);
          ofNullable(postAction).ifPresent(action -> action.accept(video));
          return just(resource);
        } catch (IOException e) {
          log.warn(e.getMessage(), e);
          throw new RuntimeException(e.getMessage(), e);
        }
      }).flatMap(Maybe::onErrorComplete);
  }

  //TODO: improve this, perhaps Resource should be null (use Optional<Resource> instead)
  private Resource writeResourceData(Boolean clean) throws IOException {
    var video = activeVideo.getVideo();
    var index = activeVideo.getResourceIndex();
    Resource resource = null;
    try {
      resource = video.getResources().get(index);
    } catch (NullPointerException | IndexOutOfBoundsException e) {
      log.warn("No resource for index '{}'", index);
    }

    boolean emptyText = clean || Objects.isNull(video) || Objects.isNull(resource);

    String name = emptyText ? "" : resource.getName();
    String url = emptyText ? "" : resource.getUrl();
    String description = emptyText ? "" : resource.getDescription();
    String summary = emptyText ? "" : resource.getSummary();
    String typeIconUrl = emptyText ? "" : resource.getType().getIconUrl();
    String typeName = emptyText ? "" : resource.getType().getName();

    dateJob.writeToFile(activeVideo.getActiveResourceTitleFile(), name);
    dateJob.writeToFile(activeVideo.getActiveResourceUrlFile(), url);
    dateJob.writeToFile(activeVideo.getActiveResourceDescriptionFile(), description);
    dateJob.writeToFile(activeVideo.getActiveResourceSummaryFile(), summary);
    dateJob.writeToFile(activeVideo.getActiveResourceTypeIconFile(), typeIconUrl);
    dateJob.writeToFile(activeVideo.getActiveResourceTypeNameFile(), typeName);

    return resource;
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
    writeResourceData(clean);
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


        String tempFile = tmpFilesDirectory + "/participantAvatarTmp" + i + "_.tmp";
        downloadFile(participant.getAvatar(), tempFile);


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

  private Boolean downloadFile(String url, String destination) {
    Boolean result = FALSE;
    try {
      FileUtils.copyURLToFile(
        new URL(url),
        new File(destination));
      result = TRUE;
    } catch (Throwable t) {
      log.warn("Cant Download avatar file from " + url, t);
    }
    return result;
  }
}
