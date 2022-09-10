package streamtimer.service;

import java.time.LocalDate;
import org.springframework.web.client.RestTemplate;
import streamtimer.model.LiturgiaCnbbApi;
import streamtimer.model.LiturgiaDiaria;

public class LiturgiaDiariaService {

  private static final String LEITURA_1 = "Leitura I (";
  private static final String LEITURA_2 = "Leitura II (";
  private static final String SALMO = "Salmo Responsorial";
  private static final String EVANGELHO = "Evangelho (";


  private static final String URL_BASE = "https://api-liturgia.edicoescnbb.com.br/contents/in/date/";

  public LiturgiaDiaria getLiturgiaDiaria(LocalDate dataLiturgia){
    String data = dataLiturgia.toString();
    LiturgiaDiaria liturgiaDiaria = new LiturgiaDiaria();
    RestTemplate restTemplate = new RestTemplate();
    try {
      LiturgiaCnbbApi liturgiaCnbbApi = restTemplate.getForObject(URL_BASE+data, LiturgiaCnbbApi.class);
      liturgiaDiaria = separarTextosLiturgia(liturgiaCnbbApi.getContent().getLeituras());
      liturgiaDiaria.setSalmoResposta(separarSalmoResposta(liturgiaCnbbApi.getContent().getBody()));
      return liturgiaDiaria;
    } catch (Exception ex){
      liturgiaDiaria.setError(true);
      return liturgiaDiaria;
    }
  }

  private LiturgiaDiaria separarTextosLiturgia(String html) throws Exception {
    LiturgiaDiaria liturgiaDiaria = new LiturgiaDiaria();
    String[] leituras = html.split("<div");
    for (int i = 0; i < leituras.length; i++) {
      String leitura = formatarTexto(leituras[i], i);
      if(i == 2){
        leitura = LEITURA_1.concat(leitura).concat(")");
        liturgiaDiaria.setLeitura1(leitura);
      } else if(i == 3) {
        leitura = SALMO.concat(leitura);
        liturgiaDiaria.setSalmo(leitura);
      } else if(i == 4) {
        leitura = LEITURA_2.concat(leitura).concat(")");
        liturgiaDiaria.setLeitura2(leitura);
      } else if(i == 5) {
        leitura = EVANGELHO.concat(leitura).concat(")");
        liturgiaDiaria.setEvangelho(leitura);
      }
    }
    return liturgiaDiaria;
  }

  private String formatarTexto (String texto, int index){
    texto = texto
            .replace("</div>","")
            .split("<br>")[0]
            .split("ou")[0]
            .split("\\)")[0]
            .replace(">","")
            .trim();

    if (index == 3){
      texto = texto.replace("Sl", "").concat(")");
    } else {
      texto = texto.split("\\(")[0].trim();
    }

    return texto;
  }

  private String separarSalmoResposta(String html) {
    try {
      String[] splitMissa = html.split("Missa do dia");
      String[] splitSalmo = null;

      if (splitMissa.length > 1) {
        splitSalmo = splitMissa[1].split("Salmo responsorial")[1]
            .split("\">R\\.( )*</font>")[1]
            .split("</div")[0]
            .split("</span>");
      } else {
        splitSalmo = html.split("Salmo responsorial")[1]
            .split("\">R\\.( )*</font>")[1]
            .split("<br><br><div>")[0]
            .split("<br>\n<br><div>")[0]
            .split("</span>");
      }

      return splitSalmo[0].replace("<br>", "")
                          .replace("<div>", "")
                          .replace("</div>", "")
                          .replace("<p>", "")
                          .replace("</p>", "")
                          .replace("&nbsp;", "")
                          .replace("  "," ")
                          .trim();
    } catch (Exception ex) {
      return "";
    }
  }

}
