package obs.util.api;

import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Status;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.multipart.PartData;
import io.reactivex.Maybe;
import lombok.AllArgsConstructor;
import obs.util.service.MarkdownConverterService;

import static io.micronaut.http.HttpStatus.CREATED;
import static io.micronaut.http.MediaType.MULTIPART_FORM_DATA;
import static io.reactivex.Maybe.just;

@Controller("/v1/markdownConverter")
@AllArgsConstructor
public class MarkdownConverterController {

    private MarkdownConverterService markdownConverterService;

    @Post
    @Status(CREATED)
    @Consumes(MULTIPART_FORM_DATA)
    public Maybe<String> transform(CompletedFileUpload video) {
        return just(video)
                .map(PartData::getBytes)
                .map(markdownConverterService::transform)
                .flatMap(Maybe::onErrorComplete);
    }
}