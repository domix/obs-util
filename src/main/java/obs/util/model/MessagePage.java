package obs.util.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MessagePage {
  private String token;
  private List<UserLiveComment> comments;
}
