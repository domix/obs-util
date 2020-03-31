package obs.util.service;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
public class YouTubeLiveMessagesRetriever implements Callable<String> {
  private final YouTubeService service;
  private final String videoId;
  private final String userId;
  private final String chatId;
  private final String token;

  public YouTubeLiveMessagesRetriever(YouTubeService service, String videoId, String userId, String chatId, String token) {
    this.service = service;
    this.videoId = videoId;
    this.userId = userId;
    this.chatId = chatId;
    this.token = token;
  }

  @Override
  public String call() throws Exception {
    try {
      service.messagesPage(videoId, userId, chatId, token);
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
    }
    return null;
  }
}
