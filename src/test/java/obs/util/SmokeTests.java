package obs.util;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.multipart.MultipartBody;
import io.micronaut.test.annotation.MicronautTest;
import io.reactivex.Maybe;
import lombok.extern.slf4j.Slf4j;
import obs.util.model.Video;
import obs.util.service.VideosService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.micronaut.http.HttpRequest.POST;
import static io.micronaut.http.HttpRequest.PUT;
import static io.micronaut.http.HttpStatus.CREATED;
import static io.micronaut.http.HttpStatus.OK;
import static io.micronaut.http.MediaType.APPLICATION_YAML_TYPE;
import static io.micronaut.http.MediaType.MULTIPART_FORM_DATA_TYPE;
import static obs.util.api.VideoController.URI;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@MicronautTest
public class SmokeTests {
  @Inject
  @Client("/")
  RxHttpClient client;
  @Inject
  VideosService videosService;

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
        POST(URI, requestBody)
          .contentType(MULTIPART_FORM_DATA_TYPE)
      );

    assertEquals(CREATED, response.getStatus());
  }

  @Test
  void shouldSetActiveVideo() throws Exception {
    log.info("Activating video test");
    var video = addTestVideo().blockingGet();
    var uri = String.format("%s/%s", URI, video.getId());

    HttpResponse<Video> response = client.toBlocking()
      .exchange(PUT(uri, ""));

    assertEquals(OK, response.getStatus());
  }

  @Test
  @Disabled
  void shouldFailWhenSetActiveVideo() throws Exception {
    /*assertThrows(NumberFormatException.class, () -> {
      Integer.parseInt("One");
    });*/

    var uri = String.format("%s/%s", URI, "foo");

    HttpResponse<Video> exchange = client.toBlocking()
      .exchange(PUT(uri, ""));

    assertEquals(OK, exchange.getStatus());
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
