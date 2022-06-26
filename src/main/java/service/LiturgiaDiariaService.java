package service;

import model.LiturgiaDiaria;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class LiturgiaDiariaService {

  private static final String LEITURA = "Leitura (";
  private static final String SALMO = "Salmo Responsorial";
  private static final String EVANGELHO = "Evangelho (";


  private static final String URL_BASE = "https://www.catolicoorante.com.br/liturgia_diaria_nocache.php?dia=";

  public LiturgiaDiaria getLiturgiaDiaria(int dia){
    LiturgiaDiaria liturgiaDiaria = new LiturgiaDiaria();

    try {
      Document doc = Jsoup.connect(URL_BASE + dia).get();

      liturgiaDiaria = separarTextosLiturgia(doc);

      return liturgiaDiaria;

    } catch (Exception ex){
      liturgiaDiaria.setError(true);
      return liturgiaDiaria;
    }

  }

  private LiturgiaDiaria separarTextosLiturgia(Document doc){
    LiturgiaDiaria liturgiaDiaria = new LiturgiaDiaria();

    //buscando leituras, evangelho e salmo
    Elements textos = doc.select("span.label-liturgia");
    textos.forEach(element -> {
      if(element.text().startsWith(LEITURA) && liturgiaDiaria.getLeitura1() == null){
        liturgiaDiaria.setLeitura1(element.text());
      } else if(element.text().startsWith(SALMO)) {
        liturgiaDiaria.setSalmo(element.text());
      } else if(element.text().startsWith(LEITURA)) {
        liturgiaDiaria.setLeitura2(element.text());
      } else if(element.text().startsWith(EVANGELHO)) {
        liturgiaDiaria.setEvangelho(element.text());
      }
    });

    //buscando salmo resposta
    Elements salmoResposta = doc.select("p[style$=:center] > strong");
    salmoResposta.forEach(element -> {
      if (salmoResposta.size() > 1) {
        if(!element.toString().startsWith("<strong><")) {
          if (liturgiaDiaria.getSalmoResposta() == null) {
            liturgiaDiaria.setSalmoResposta(element.text());
          } else {
            liturgiaDiaria.setSalmoResposta(liturgiaDiaria.getSalmoResposta().concat("\n").concat(element.text()));
          }
        }
      } else {
        if(!element.toString().startsWith("<strong><")) {
          liturgiaDiaria.setSalmoResposta(element.text());
        }
      }
    });

    if (textos.size() == 0 && salmoResposta.size() == 0){
      liturgiaDiaria.setError(true);
    }

    return liturgiaDiaria;

  }

}
