package obs.util;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.multipart.MultipartBody;
import io.micronaut.test.annotation.MicronautTest;
import obs.util.model.Video;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.nio.file.Paths;

import static io.micronaut.http.HttpRequest.POST;
import static io.micronaut.http.HttpStatus.CREATED;
import static io.micronaut.http.MediaType.APPLICATION_YAML_TYPE;
import static io.micronaut.http.MediaType.MULTIPART_FORM_DATA_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
public class SmokeTests {
  @Inject
  @Client("/")
  RxHttpClient client;

  @Test
  void addFile() throws Exception {

    var theFile = Paths
      .get(getClass().getResource("/video.yaml").toURI())
      .toFile();

    var requestBody = MultipartBody.builder()
      .addPart(
        "video",
        theFile.getName(),
        APPLICATION_YAML_TYPE,
        theFile
      ).build();

    HttpResponse<Video> response = client.toBlocking()
      .exchange(
        POST("/v1/videos", requestBody)
          .contentType(MULTIPART_FORM_DATA_TYPE)
      );

    assertEquals(CREATED, response.getStatus());
  }
}
