package streamtimer.model;

import java.io.Serializable;

public class LiturgiaCnbbApi implements Serializable {

  private Content content;

  public LiturgiaCnbbApi() {
  }

  public LiturgiaCnbbApi(Content content) {
    this.content = content;
  }

  public Content getContent() {
    return content;
  }

  public void setContent(Content content) {
    this.content = content;
  }
}
