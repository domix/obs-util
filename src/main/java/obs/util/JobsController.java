package obs.util;

import io.micronaut.http.annotation.*;

import static io.micronaut.http.HttpStatus.NO_CONTENT;

@Controller("/v1/jobs")
public class JobsController {
  private final DateJob dateJob;

  public JobsController(DateJob dateJob) {
    this.dateJob = dateJob;
  }

  @Post
  public FileProps create(@Body FileProps data) {
    System.out.println(data.toString());
    dateJob.foo(data);
    return data;
  }

  @Delete("/{id}")
  @Status(NO_CONTENT)
  public void delete(@PathVariable String id) {
    dateJob.fff(id);
  }
}
