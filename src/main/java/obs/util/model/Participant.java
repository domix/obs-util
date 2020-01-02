package obs.util.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static obs.util.model.Role.HOST;

@Getter
@Setter
@ToString
public class Participant {
  private Role role = HOST;
  private String name;
  private String twitter;
  private String github;
  private String company;
  private String companyTitle;
}
