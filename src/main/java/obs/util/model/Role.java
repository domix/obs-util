package obs.util.model;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum Role {
  HOST("host"), GUEST("guest"), PRODUCER("producer");
  private final String name;

  Role(String name) {
    this.name = name;
  }
}
