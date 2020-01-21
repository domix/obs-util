package obs.util.service;

import io.micronaut.core.util.CollectionUtils;
import io.reactivex.Maybe;
import lombok.extern.slf4j.Slf4j;
import net.steppschuh.markdowngenerator.MarkdownBuilder;
import net.steppschuh.markdowngenerator.list.ListBuilder;
import net.steppschuh.markdowngenerator.text.TextBuilder;
import net.steppschuh.markdowngenerator.text.heading.Heading;
import obs.util.model.Participant;
import obs.util.model.Resource;
import obs.util.model.ResourceType;
import obs.util.model.Video;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Singleton;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.reactivex.Maybe.just;

@Slf4j
@Singleton
public class MarkdownConverterService {

  private final Yaml yaml;

  public MarkdownConverterService() {
    yaml = new Yaml();
  }

  public Maybe<String> transform(byte[] bytes) {
    log.info("About to transform video from bytes..");
    return just(bytes)
            .map(this::loadDataAsVideo)
            .map(this::transformToMarkdown);
  }

  public Video loadDataAsVideo(byte[] bytes) {
    try (var is = new ByteArrayInputStream(bytes)) {
      Video video = yaml.load(is);
      log.info("Loaded video id: '{}', title: {} from bytes.", video.getId(), video.getShowTitle());
      return video;
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage(), t);
    }
  }

  public String transformToMarkdown(Video video) {
    log.info("Starting transformation");
    MarkdownBuilder markdownBuilder = new TextBuilder();
    Heading header = new Heading(String.format("%s: %s", video.getShowTitle(), video.getShowSubtitle()));
    header.setUnderlineStyle(false);
    markdownBuilder.append(header)
            .newParagraph();

    List<String> participantList = findAllTwitterParticipants(video);
    String participantStr = String.join(", ", participantList);
    markdownBuilder.beginList()
            .append(String.format("Conducido por %s", participantStr))
            .end()
            .newParagraph();

    transformNewsSection(markdownBuilder, video);
    transformTwitterSection(markdownBuilder, video);
    transformReferencesAndResourcesSection(markdownBuilder, video);
    transformCoderepositoriesSection(markdownBuilder, video);
    transformEventsSection(markdownBuilder, video);

    log.info("Markdown builder done!");

    return markdownBuilder.toString();
  }

  private void transformSection( MarkdownBuilder markdownBuilder, Video video,
                                 String sectionName, ResourceType resourceType) {


    List<Resource> resourceList = findAllResourcesByType(resourceType, video);

    if (CollectionUtils.isEmpty(resourceList)) {
      return;
    }

    Heading sectionHeader = new Heading(sectionName, 2);
    sectionHeader.setUnderlineStyle(false);
    markdownBuilder.append(sectionHeader)
      .newParagraph();

    ListBuilder markdownListBuilder = markdownBuilder.beginList();

    for (Resource resource : resourceList) {
      markdownListBuilder
              .link(resource.getName(), resource.getUrl());
    }
    markdownListBuilder
            .end()
            .newParagraph();
  }

  private void transformNewsSection(MarkdownBuilder markdownBuilder, Video video) {
    transformSection(markdownBuilder, video,
            "Revisión de las noticias", ResourceType.NEWS_ITEM);
  }

  private void transformTwitterSection(MarkdownBuilder markdownBuilder, Video video) {
    transformSection(markdownBuilder, video,
            "Twitter!", ResourceType.TWEET);
  }

  private void transformReferencesAndResourcesSection(MarkdownBuilder markdownBuilder, Video video) {
    transformSection(markdownBuilder, video,
            "Referencias y Recursos", ResourceType.REFERENCE);
  }

  private void transformCoderepositoriesSection(MarkdownBuilder markdownBuilder, Video video) {
    transformSection(markdownBuilder, video,
            "Repos chingones de código", ResourceType.SOURCE_CODE_REPO);
  }

  private void transformEventsSection(MarkdownBuilder markdownBuilder, Video video) {
    transformSection(markdownBuilder, video,
            "Eventos", ResourceType.EVENT);
  }

  private List<String> findAllTwitterParticipants(Video video) {
    if (Objects.isNull(video) ||
            CollectionUtils.isEmpty(video.getParticipants())) {
      return List.of();
    }

    return video.getParticipants()
            .stream()
            .map(Participant::getTwitter)
            .map(t -> "@" + t)
            .collect(Collectors.toList());
  }

  private List<Resource> findAllResourcesByType(ResourceType resourceType, Video video) {
    if (Objects.isNull(video) || Objects.isNull(video.getResources())) {
      List.of();
    }

    return video.getResources().stream()
            .filter(r -> resourceType == r.getType())
            .collect(Collectors.toList());

  }
}
