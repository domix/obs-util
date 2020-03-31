package obs.util.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import io.micronaut.scheduling.TaskScheduler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import obs.util.model.MessagePage;
import obs.util.model.UserLiveComment;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.load;
import static com.google.api.services.youtube.YouTubeScopes.all;
import static com.google.common.collect.Lists.newArrayList;
import static java.time.Duration.ofMillis;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Slf4j
@Singleton
public class YouTubeService {
  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  public static final String APPLICATION_NAME = "obs-util";
  private  VideosService videosService;
  private final TaskScheduler taskScheduler;
  private final ApplicationContext applicationContext;

  public YouTubeService(TaskScheduler taskScheduler, ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
    this.taskScheduler = taskScheduler;
  }

  @EventListener
  void onStartup(ServerStartupEvent event) {
    videosService = applicationContext.getBean(VideosService.class);
  }


  @SneakyThrows
  public Credential authorize(String userId) {
    var scopes = newArrayList(all());
    var resourceAsStream = getClass().getResourceAsStream("/client_secrets.json");
    var clientSecrets = load(JSON_FACTORY, new InputStreamReader(resourceAsStream));
    var pathname = System.getProperty("user.home") + "/.credentials/";

    var fileDataStoreFactory =
      new FileDataStoreFactory(
        new File(pathname));

    var flow =
      new GoogleAuthorizationCodeFlow.Builder(
        HTTP_TRANSPORT,
        JSON_FACTORY,
        clientSecrets,
        scopes)
        .setDataStoreFactory(fileDataStoreFactory)
        .build();

    var localReceiver = new LocalServerReceiver
      .Builder()
      .setPort(8081)
      .build();

    Credential authorize =
      new AuthorizationCodeInstalledApp(flow, localReceiver)
        .authorize(userId);

    boolean b = authorize.refreshToken();
    log.trace("Se pudo refrescar el token? {}", b);
    return authorize;
  }

  @SneakyThrows
  public List<Map> videosDataFor(String userId) {
    var youtube = createYouTube(userId);

    var channelRequest =
      youtube
        .channels()
        .list("contentDetails");

    channelRequest.setMine(true);
    channelRequest.setFields("items/contentDetails,nextPageToken,pageInfo");

    var channelResult = channelRequest.execute();
    var channelsList = channelResult.getItems();

    if (channelsList != null && channelsList.size() > 0) {
      var uploadPlaylistId =
        channelsList.get(0)
          .getContentDetails()
          .getRelatedPlaylists()
          .getUploads();

      var playlistItemList = new ArrayList<PlaylistItem>();

      var playlistItemRequest =
        youtube.playlistItems()
          .list("id,contentDetails,snippet");

      playlistItemRequest.setPlaylistId(uploadPlaylistId);

      playlistItemRequest.setFields(
        "items(contentDetails/videoId,snippet/title,snippet/publishedAt),nextPageToken,pageInfo");

      String nextToken = "";

      do {
        playlistItemRequest.setPageToken(nextToken);
        PlaylistItemListResponse playlistItemResult = playlistItemRequest.execute();

        playlistItemList.addAll(playlistItemResult.getItems());

        nextToken = playlistItemResult.getNextPageToken();
      } while (nextToken != null);

      return prettyPrint(playlistItemList);
    } else {
      return emptyList();
    }

      /*


      String nextToken = "";*/

  }

  @SneakyThrows
  public Optional<LiveBroadcast> broadcast(String userId, String broadcastId) {
    log.info("Buscando broadcat del usuario {}, con id: {}", userId, broadcastId);
    var youtube = createYouTube(userId);

    var broadcasts = youtube.liveBroadcasts()
      .list("id,snippet,contentDetails,status");

    broadcasts.setBroadcastStatus("all");
    broadcasts.setBroadcastType("all");
    broadcasts.setMaxResults(50l);

    var broadcastsResult = broadcasts.execute();
    var broadcastsList = broadcastsResult.getItems();

    return broadcastsList.stream()
      .filter(liveBroadcast ->
        liveBroadcast.getId()
          .equals(broadcastId))
      .findFirst();
  }

  @SneakyThrows
  public void broadcast(String userId) {
    var youtube = createYouTube(userId);

    var broadcasts = youtube.liveBroadcasts()
      .list("id,snippet,contentDetails,status");

    broadcasts.setBroadcastStatus("active");
    broadcasts.setBroadcastType("all");

    var broadcastsResult = broadcasts.execute();
    var broadcastsList = broadcastsResult.getItems();

    if (broadcastsList != null && broadcastsList.size() > 0) {
      broadcastsList.forEach(broadcast -> {
        System.out.println("\n\n");
        System.out.println("id: " + broadcast.getId());
        System.out.println(broadcast.getSnippet().getDescription());
        System.out.println(broadcast.getSnippet().getTitle());
        System.out.println("LiveChatId: " + broadcast.getSnippet().getLiveChatId());
        System.out.println(broadcast.getStatus().getLifeCycleStatus());
      });
    }
  }

  @SneakyThrows
  public void liveChatMessages(String userId, String liveChatId) {
    var youTube = createYouTube(userId);
    var s = "id,snippet,authorDetails";
    String token = null;


    var list = youTube.liveChatMessages()
      .list(liveChatId, s);
    list.setPageToken(token);
    list.setMaxResults(200l);
    LiveChatMessageListResponse execute = list.execute();

    token = execute.getNextPageToken();
    Long pollingIntervalMillis = execute.getPollingIntervalMillis();
    List<LiveChatMessage> items = execute.getItems();

    log.trace("next polling: {}", pollingIntervalMillis);
    items.forEach(liveChatMessage -> {
      LiveChatMessageAuthorDetails authorDetails = liveChatMessage.getAuthorDetails();
      System.out.println(authorDetails.getDisplayName());
      System.out.println(authorDetails.getProfileImageUrl());
      System.out.println(liveChatMessage.getSnippet().getDisplayMessage());
    });
  }

  private YouTube createYouTube(String userId) {
    YouTube build = new YouTube
      .Builder(HTTP_TRANSPORT, JSON_FACTORY, authorize(userId))
      .setApplicationName(APPLICATION_NAME)
      .build();

    return build;
  }

  @SneakyThrows
  public void foo(String userId) {
    var youtube = createYouTube(userId);

    var requestLiveStreams =
      youtube
        .liveStreams()
        .list("id,contentDetails,snippet");
    //YouTube.LiveChatMessages.List requestLiveChatMessages = youtube.liveChatMessages().list("", "");
    requestLiveStreams.setMaxResults(50l);
    requestLiveStreams.setMine(true);
    requestLiveStreams.setFields("items/contentDetails,nextPageToken,pageInfo");

    var channelResult = requestLiveStreams.execute();
    var channelsList = channelResult.getItems();

    if (channelsList != null && channelsList.size() > 0) {
      channelsList.forEach(liveStream -> {
        System.out.println("id: " + liveStream.getId());
        System.out.println(liveStream.getSnippet().getDescription());
      });
    }
    /*
    if (channelsList != null && channelsList.size() > 0) {
      var uploadPlaylistId =
        channelsList.get(0)
          .getContentDetails();

      var playlistItemList = new ArrayList<PlaylistItem>();

      var playlistItemRequest =
        youtube.playlistItems()
          .list("id,contentDetails,snippet");

      playlistItemRequest.setPlaylistId(uploadPlaylistId);

      playlistItemRequest.setFields(
        "items(contentDetails/videoId,snippet/title,snippet/publishedAt),nextPageToken,pageInfo");

      String nextToken = "";

      do {
        playlistItemRequest.setPageToken(nextToken);
        PlaylistItemListResponse playlistItemResult = playlistItemRequest.execute();

        playlistItemList.addAll(playlistItemResult.getItems());

        nextToken = playlistItemResult.getNextPageToken();
      } while (nextToken != null);

     */
    //return null;
  }

  private List<Map> prettyPrint(List<PlaylistItem> playlistEntries) {
    return playlistEntries
      .stream()
      .map(playlistItem ->
        Map.of(
          "name", playlistItem.getSnippet().getTitle(),
          "id", playlistItem.getContentDetails().getVideoId(),
          "date", playlistItem.getSnippet().getPublishedAt().toStringRfc3339()))
      .collect(toList());
  }

  public Optional<String> getLiveChatIdForVideo(String userId, String videoId) {
    log.info("Intentando obtener mensajes de {} del video {}", userId, videoId);
    try {
      return ofNullable(
        broadcast(userId, videoId)
          .map(liveBroadcast ->
            liveBroadcast.getSnippet().getLiveChatId())
          .orElse(null));
    } catch (Throwable t) {
      log.error(t.getMessage(), t);
      throw new RuntimeException(t);
    }

  }

  public MessagePage messagesPage(String videoId, String userId, String liveChatId, String token) {
    log.trace("Retrieveing comment for {}, using token {}", liveChatId, token);
    var youTube = createYouTube(userId);
    var s = "id,snippet,authorDetails";

    YouTube.LiveChatMessages.List list = null;
    try {
      list = youTube.liveChatMessages()
        .list(liveChatId, s);
    } catch (IOException e) {
      scheduleNextChatMessagesRetriever(videoId, userId, liveChatId, token, 3000l);
      throw new RuntimeException(e);
    }
    list.setPageToken(token);
    list.setMaxResults(200l);
    list.setProfileImageSize(720l);
    LiveChatMessageListResponse execute = null;
    Long pollingIntervalMillis = 3000l;
    List<LiveChatMessage> items = emptyList();
    try {
      execute = list.execute();
      token = execute.getNextPageToken();
      pollingIntervalMillis = execute.getPollingIntervalMillis();
      items = execute.getItems();
    } catch (IOException e) {
      pollingIntervalMillis = 3000l;
      items = emptyList();
      log.error(e.getMessage(), e);
    }

    log.trace("next polling in {} millis", pollingIntervalMillis);

    List<UserLiveComment> comments = items.stream()
      .map(liveChatMessage ->
        UserLiveComment.builder()
          //TODO: set this
          .source("YouTube")
          .sourceLogo("")
          .sourceCommentId(liveChatMessage.getId())
          .userFullName(liveChatMessage.getAuthorDetails().getDisplayName())
          .userImageUrl(liveChatMessage.getAuthorDetails().getProfileImageUrl())
          .userMessage(liveChatMessage.getSnippet().getDisplayMessage())
          .build()).collect(toList());
    //kfhjds

    log.trace("Comentarios encontrados: {}", comments.size());

    comments.forEach(userLiveComment -> {
      videosService.addUserComment(videoId, userLiveComment);
    });
    scheduleNextChatMessagesRetriever(videoId, userId, liveChatId, token, pollingIntervalMillis);

    return MessagePage.builder()
      .comments(comments)
      .token(token)
      .build();
  }

  private void scheduleNextChatMessagesRetriever(String videoId, String userId, String liveChatId, String token, Long pollingIntervalMillis) {
    taskScheduler.schedule(
      ofMillis(pollingIntervalMillis + 10000),
      new YouTubeLiveMessagesRetriever(
        this,
        videoId,
        userId,
        liveChatId,
        token));
  }
}
