package streamtimer.model;

public class LiturgiaDiaria {

  private String leitura1;
  private String salmo;
  private String salmoResposta;
  private String leitura2;
  private String evangelho;
  private Boolean error = false;

  public String getLeitura1() {
    return leitura1;
  }

  public void setLeitura1(String leitura1) {
    this.leitura1 = leitura1;
  }

  public String getSalmo() {
    return salmo;
  }

  public void setSalmo(String salmo) {
    this.salmo = salmo;
  }

  public String getSalmoResposta() {
    return salmoResposta;
  }

  public void setSalmoResposta(String salmoResposta) {
    this.salmoResposta = salmoResposta;
  }

  public String getLeitura2() {
    return leitura2;
  }

  public void setLeitura2(String leitura2) {
    this.leitura2 = leitura2;
  }

  public String getEvangelho() {
    return evangelho;
  }

  public void setEvangelho(String evangelho) {
    this.evangelho = evangelho;
  }

  public Boolean getError() {
    return error;
  }

  public void setError(Boolean error) {
    this.error = error;
  }

}
