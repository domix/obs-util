package obs.util.api;

import io.micronaut.http.annotation.*;
import obs.util.model.FileProps;
import obs.util.service.DateJob;

import java.util.List;

import static io.micronaut.http.HttpStatus.NO_CONTENT;

@Controller("/v1/jobs")
public class JobsController {
  private final DateJob dateJob;

  public JobsController(DateJob dateJob) {
    this.dateJob = dateJob;
  }

  @Post
  public FileProps create(@Body FileProps data) {
    return dateJob.schedule(data);
  }

  @Delete("/{id}")
  @Status(NO_CONTENT)
  public void delete(@PathVariable String id) {
    dateJob.removeTask(id);
  }

  @Get
  public List<FileProps> list() {
    return dateJob.allTasks();
  }
}
