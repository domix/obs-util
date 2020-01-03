package obs.util.api;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.client.multipart.MultipartBody;
import io.micronaut.test.annotation.MicronautTest;
import io.reactivex.Maybe;
import lombok.extern.slf4j.Slf4j;
import obs.util.model.ActiveVideo;
import obs.util.model.Video;
import obs.util.service.VideosService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.micronaut.http.HttpRequest.*;
import static io.micronaut.http.HttpStatus.*;
import static io.micronaut.http.MediaType.APPLICATION_YAML_TYPE;
import static io.micronaut.http.MediaType.MULTIPART_FORM_DATA_TYPE;
import static obs.util.api.VideoController.ACTIVE_VIDEO_URI;
import static obs.util.api.VideoController.BASE_URI_VIDEOS;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@MicronautTest
public class VideoControllerTests {
  @Inject
  @Client("/")
  RxHttpClient client;
  @Inject
  VideosService videosService;

  @BeforeEach
  void init() {
    videosService.resetStorage();
  }

  @Test
  void shouldAddFile() throws Exception {

    var theFile = videoFile().toFile();

    var requestBody = MultipartBody.builder()
      .addPart(
        "video",
        theFile.getName(),
        APPLICATION_YAML_TYPE,
        theFile
      ).build();

    HttpResponse<Video> response = client.toBlocking()
      .exchange(
        POST(BASE_URI_VIDEOS, requestBody)
          .contentType(MULTIPART_FORM_DATA_TYPE)
      );

    assertEquals(CREATED, response.getStatus());
  }

  @Test
  void shouldSetActiveVideo() throws Exception {
    log.info("Activating video test");
    var video = addTestVideo().blockingGet();
    var uri = String.format("%s/%s", BASE_URI_VIDEOS, video.getId());

    HttpResponse<Video> response = client.toBlocking()
      .exchange(PUT(uri, ""));

    assertEquals(OK, response.getStatus());
  }

  @Test
  void shouldFailWhenSetActiveVideo() throws Exception {
    var uri = String.format("%s/%s", BASE_URI_VIDEOS, "foo");

    assertThrows(HttpClientResponseException.class, () -> {
      HttpResponse<Video> response = client.toBlocking()
        .exchange(PUT(uri, ""));

      assertEquals(NOT_FOUND, response.getStatus());
    });
  }

  @Test
  void shouldGetActiveVideo() throws Exception {

    HttpResponse<ActiveVideo> response = client.toBlocking()
      .exchange(GET(BASE_URI_VIDEOS + ACTIVE_VIDEO_URI), ActiveVideo.class);

    assertTrue(response.getBody().isPresent());
    ActiveVideo activeVideo = response.getBody().get();
    assertNull(activeVideo.getVideo());
    assertEquals(OK, response.getStatus());

    Video video = addTestVideo().blockingGet();
    videosService.setVideoActive(video.getId()).blockingGet();

    response = client.toBlocking()
      .exchange(GET(BASE_URI_VIDEOS + ACTIVE_VIDEO_URI), ActiveVideo.class);

    activeVideo = response.getBody().get();
    assertNotNull(activeVideo.getVideo());
    assertEquals(OK, response.getStatus());

  }

  private Maybe<Video> addTestVideo() throws Exception {
    var bytes = Files.readAllBytes(videoFile());
    return videosService.add(bytes);
  }

  private Path videoFile() throws Exception {
    return Paths.get(
      getClass().getResource("/video.yaml").toURI()
    );
  }
}
