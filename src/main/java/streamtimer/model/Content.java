package streamtimer.model;

import java.io.Serializable;

public class Content implements Serializable {

  private String leituras;

  private String body;

  public Content() {
  }

  public Content(String leituras, String body) {
    this.leituras = leituras;
    this.body = body;
  }

  public String getLeituras() {
    return leituras;
  }

  public void setLeituras(String leituras) {
    this.leituras = leituras;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }
}
