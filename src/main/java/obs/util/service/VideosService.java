package obs.util.service;

import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import io.reactivex.Maybe;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import obs.util.model.*;
import org.apache.commons.imaging.*;
import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static io.reactivex.Maybe.just;
import static java.awt.AlphaComposite.Clear;
import static java.awt.AlphaComposite.Src;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Slf4j
@Singleton
public class VideosService {
  public static final String BLANK_STRING = "";
  private final ConcurrentMap<String, Video> storage = new ConcurrentHashMap<>();
  private final Yaml yaml;
  private final DateJob dateJob;
  private final MarkdownConverterService markdownConverterService;
  private ActiveVideo activeVideo = new ActiveVideo();
  @Getter
  private String baseDirectory;
  private String tmpFilesDirectory;

  public VideosService(DateJob dateJob, @Value("${basedir:~/.obs-util}") String baseDir, MarkdownConverterService markdownConverterService) throws IOException {
    this.markdownConverterService = markdownConverterService;
    if (baseDir.startsWith("~/")) {
      String replace = baseDir.replace("~/", BLANK_STRING);
      String home = System.getProperty("user.home");
      baseDirectory = home + "/" + replace;
    } else {
      baseDirectory = baseDir;
    }

    this.dateJob = dateJob;
    yaml = new Yaml();
    tmpFilesDirectory = baseDirectory + "/.tmp";
    log.info("Using base dir '{}'", baseDirectory);

    Path baseDirectoryPath = Paths.get(baseDirectory);
    Path tmpDirectoryPath = Paths.get(tmpFilesDirectory);
    Files.createDirectories(baseDirectoryPath);
    Files.createDirectories(tmpDirectoryPath);
    activeVideo.setBaseWorkDir(baseDirectory);
  }

  @SneakyThrows
  @EventListener
  void onStartup(ServerStartupEvent event) {
    generateGeneralEmptyFiles();
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
      .map(this::addToStorage)
      .map(this::saveToMarkdown);
  }

  public Video createDateJob(Video video) throws Exception {
    String startTimeFile = activeVideo.getStartTimeFile();
    dateJob.writeToFile(startTimeFile, BLANK_STRING);

    FileProps timeInfo = video.getStartTimeInfo();
    if (nonNull(timeInfo)) {
      log.info("Video '{}' with start date.", video.getId());
      log.info("FileProps: {}", timeInfo.toString());
      FileProps build = FileProps.builder()
        .cronExpression(timeInfo.getCronExpression())
        .outputFormat(timeInfo.getOutputFormat())
        .destination(startTimeFile)
        .dateFormatPattern(timeInfo.getDateFormatPattern())
        .id(video.getId())
        .prefix(timeInfo.getPrefix())
        .startedMessage(timeInfo.getStartedMessage())
        .startTime(video.getStartTimeInfo().getStartTime())
        .timeZone(timeInfo.getTimeZone())
        .build();

      log.info("FileProps: {}", build.toString());

      dateJob.schedule(build);
    } else {
      log.info("No tiene satrt time");
    }
    return video;
  }

  public Video addToStorage(Video video) {
    storage.put(video.getId(), video);
    log.info("Video '{}' added to storage.", video.getId());
    return video;
  }

  public Video saveToMarkdown(Video video) throws Exception {
    var markdown = markdownConverterService.transformToMarkdown(video);
    dateJob.writeToFile(activeVideo.getMarkdownFile(), markdown);
    log.info("Markdown file written.");
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
    log.info("Video found in storage? {}", nonNull(video));
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
  private Resource writeResourceData(Boolean clean) throws IOException, ImageWriteException, ImageReadException {
    var video = activeVideo.getVideo();
    var index = activeVideo.getResourceIndex();
    Resource resource = null;
    try {
      resource = video.getResources().get(index);
    } catch (NullPointerException | IndexOutOfBoundsException e) {
      log.warn("No resource for index '{}'", index);
    }

    boolean emptyText = clean || Objects.isNull(video) || Objects.isNull(resource);
    ResourceType resourceType = resource.getType();
    long count = video.getResources().stream()
      .filter(resource1 -> resource1.getType().equals(resourceType)).count();
    int size = video.getResources().size();
    long l = index + 1;

    log.info("De los recursos de tipo {}, hay {} elementos", resourceType.getName(), count);
    log.info("Elementos totales {}", size);
    log.info("({}-{})", l, count);

    String name = emptyText ? BLANK_STRING : resource.getName();
    String url = emptyText ? BLANK_STRING : resource.getUrl();
    String description = emptyText ? BLANK_STRING : resource.getDescription();
    String summary = emptyText ? BLANK_STRING : resource.getSummary();
    String typeIconUrl = emptyText ? BLANK_STRING : resource.getType().getIconUrl();
    String typeName = emptyText ? BLANK_STRING : resource.getType().getName();

    dateJob.writeToFile(activeVideo.getActiveResourceTitleFile(), name);
    dateJob.writeToFile(activeVideo.getActiveResourceUrlFile(), url);
    dateJob.writeToFile(activeVideo.getActiveResourceDescriptionFile(), description);
    dateJob.writeToFile(activeVideo.getActiveResourceSummaryFile(), summary);
    dateJob.writeToFile(activeVideo.getActiveResourceTypeIconFile(), typeIconUrl);
    dateJob.writeToFile(activeVideo.getActiveResourceTypeNameFile(), typeName);

    String resourceTypeAvatarFile = activeVideo.getResourceTypeAvatarFile(resource.getType());
    imageFile(resourceTypeAvatarFile, activeVideo.getActiveResourceTypeAvatarFile(), false, clean, 200);

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

  public void writeActiveVideoInfo(Boolean clean) throws Exception {
    Video video = activeVideo.getVideo();
    if (nonNull(video)) {
      writeActiveVideoInfo(video, clean);
    }
    writeResourceData(clean);
  }

  public void writeActiveVideoInfo(Video video, Boolean clean) throws Exception {
    log.info("About to write general files...");

    String showName = clean ? BLANK_STRING : video.getShowName();
    String showTitle = clean ? BLANK_STRING : video.getShowTitle();
    String showSubtitle = clean ? BLANK_STRING : video.getShowSubtitle();

    dateJob.writeToFile(activeVideo.getShowNameFile(), showName);
    dateJob.writeToFile(activeVideo.getShowTitleFile(), showTitle);
    dateJob.writeToFile(activeVideo.getShowSubtitleFile(), showSubtitle);

    String tempShowFile = tmpFilesDirectory + "/showLogo.tmp";
    Boolean success = downloadFile(video.getLogoUrl(), tempShowFile);

    if (success) {
      try {
        imageFile(tempShowFile, activeVideo.getShowLogoFile(), video.getLogoCircled(), clean, 400);
      } catch (Exception e) {
        log.warn(e.getMessage(), e);
        //TODO: generate a empty png file when this fails...
      }
    } else {
      //TODO: generate a empty png file when the logo is empty.
    }

    createDateJob(video);

    log.info("General files has been written.");

    log.info("About to write Participant files...");
    for (int i = 0; i < video.getParticipants().size(); i++) {
      try {
        Participant participant = video.getParticipants().get(i);
        String roleName = clean ? BLANK_STRING : participant.getRole().getName();
        String name = clean ? BLANK_STRING : participant.getName();
        String twitter = clean ? BLANK_STRING : participant.getTwitter();
        String github = clean ? BLANK_STRING : participant.getGithub();
        String company = clean ? BLANK_STRING : participant.getCompany();
        String companyTitle = clean ? BLANK_STRING : participant.getCompanyTitle();
        String tempFile = tmpFilesDirectory + "/participantAvatarTmp" + i + "_.tmp";
        downloadFile(participant.getAvatar(), tempFile);

        imageFile(tempFile, activeVideo.getParticipantAvatarFile(i), participant.getCircled(), clean, 200);

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

  @SneakyThrows
  private void generateGeneralEmptyFiles() {
    dateJob.writeToFile(activeVideo.getShowNameFile(), BLANK_STRING);
    dateJob.writeToFile(activeVideo.getShowTitleFile(), BLANK_STRING);
    dateJob.writeToFile(activeVideo.getShowSubtitleFile(), BLANK_STRING);

    String name = "/transparent.png";
    InputStream inputStream = this.getClass().getResourceAsStream(name);
    Path path = Paths.get(activeVideo.getTransparentImage());
    Files.copy(inputStream, path);


    for (int i = 0; i < 5; i++) {
      try {
        imageFile(activeVideo.getTransparentImage(), activeVideo.getParticipantAvatarFile(i), false, false, 200);

        dateJob.writeToFile(activeVideo.getParticipantRoleFile(i), BLANK_STRING);
        dateJob.writeToFile(activeVideo.getParticipantNameFile(i), BLANK_STRING);
        dateJob.writeToFile(activeVideo.getParticipantTwitterFile(i), BLANK_STRING);
        dateJob.writeToFile(activeVideo.getParticipantGitHubFile(i), BLANK_STRING);
        dateJob.writeToFile(activeVideo.getParticipantCompanyFile(i), BLANK_STRING);
        dateJob.writeToFile(activeVideo.getParticipantCompanyTitleFile(i), BLANK_STRING);
      } catch (Throwable t) {
        log.error(t.getMessage(), t);
      }
    }
    dateJob.writeToFile(activeVideo.getActiveResourceTitleFile(), BLANK_STRING);
    dateJob.writeToFile(activeVideo.getActiveResourceUrlFile(), BLANK_STRING);
    dateJob.writeToFile(activeVideo.getActiveResourceDescriptionFile(), BLANK_STRING);
    dateJob.writeToFile(activeVideo.getActiveResourceSummaryFile(), BLANK_STRING);
    dateJob.writeToFile(activeVideo.getActiveResourceTypeIconFile(), BLANK_STRING);
    dateJob.writeToFile(activeVideo.getActiveResourceTypeNameFile(), BLANK_STRING);

    imageFile(activeVideo.getTransparentImage(), activeVideo.getShowLogoFile(), false, false, 400);
    imageFile(activeVideo.getTransparentImage(), activeVideo.getActiveResourceTypeAvatarFile(), false, false, 200);

    String startTimeFile = activeVideo.getStartTimeFile();
    dateJob.writeToFile(startTimeFile, BLANK_STRING);
  }

  private Boolean downloadFile(String url, String destination) {
    log.info("About to download file from '{}'", url);
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


  public File imageFile(String sourceFile, String destination, Boolean circled, Boolean clean, int size) throws
    ImageReadException, ImageWriteException, IOException {
    File file = new File(sourceFile);

    final BufferedImage image = Imaging.getBufferedImage(file);
    final ImageFormat format = ImageFormats.PNG;
    final Map<String, Object> params = new HashMap<>();
    int resize = size;
    BufferedImage resized = resize(image, resize, resize);
    BufferedImage circledAvatar = circled ? circle(resized, resize, clean) : resized;

    File destinationFile = new File(destination);
    BufferedImage canvas = canvas(size, circledAvatar, clean);

    Imaging.writeImage(canvas, destinationFile, format, params);

    return destinationFile;
  }

  public BufferedImage canvas(int width, BufferedImage avatar, Boolean clean) {
    int height = width;
    // Constructs a BufferedImage of one of the predefined image types.
    BufferedImage bufferedImage = new BufferedImage(width, height, TYPE_INT_ARGB);

    // Create a graphics which can be used to draw into the buffered image
    Graphics2D g2d = bufferedImage.createGraphics();

    // fill all the image with white
    g2d.setComposite(Clear);
    g2d.fillRect(0, 0, width, height);
    g2d.setComposite(Src);

    if (!clean) {
      RescaleOp rop = new RescaleOp(1f, 4f, null);
      g2d.drawImage(avatar, rop, 0, 0);
    }

    // Disposes of this graphics context and releases any system resources that it is using.
    g2d.dispose();

    return bufferedImage;
  }

  public BufferedImage circle(BufferedImage bufferedImage, int width, Boolean clean) {
    BufferedImage circleBuffer = new BufferedImage(width, width, TYPE_INT_ARGB);
    Graphics2D g2 = circleBuffer.createGraphics();
    g2.setClip(new Ellipse2D.Float(0, 0, width, width));
    if (!clean) {
      g2.drawImage(bufferedImage, 0, 0, width, width, null);
    }

    g2.dispose();

    return circleBuffer;
  }

  public BufferedImage resize(BufferedImage img, int newW, int newH) throws IOException {
    return Thumbnails.of(img)
      .size(newW, newH)
      .asBufferedImage();
  }

  @PostConstruct
  public void loadResourceTypesIcons() {
    Stream.of(ResourceType.values()).parallel().forEach(resourceType -> {
      String resourceFile = activeVideo.getResourceTypeAvatarFile(resourceType);
      downloadFile(resourceType.getIconUrl(), resourceFile);
    });
  }
}
