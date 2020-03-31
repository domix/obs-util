package obs.util.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class UserLiveComment {
  private String source;
  private String sourceLogo;
  private String sourceCommentId;
  private String userFullName;
  private String userImageUrl;
  @Setter
  private String userLocalImageFileName;
  private String userMessage;
}
