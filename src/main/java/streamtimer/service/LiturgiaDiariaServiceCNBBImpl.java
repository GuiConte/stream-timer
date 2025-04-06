package streamtimer.service;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.web.client.RestTemplate;
import streamtimer.model.LiturgiaCnbbApi;
import streamtimer.model.LiturgiaDiaria;

public class LiturgiaDiariaServiceCNBBImpl implements LiturgiaDiariaService{

  private static final String LEITURA_1 = "Leitura I (";
  private static final String LEITURA_2 = "Leitura II (";
  private static final String SALMO = "Salmo Responsorial ";
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
        leitura = SALMO.concat(leitura).replace("  ", " ");
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
      /**
       * Para dias em que possuir mais de uma missa no dia, procura sempre a liturgia referente a missa do dia.
       * começando a busca a partir do texto "Salmo Responsorial"
       */
      String[] splitMissa = html.split("Missa do dia");
      String[] splitSalmo = null;

      if (splitMissa.length > 1) {
        splitSalmo = splitMissa[1].split("Salmo responsorial");
      } else {
        splitSalmo = html.split("Salmo responsorial");
      }

      /**
       * Interrompe a busca a partir do momento em que encontrar uma frase iniciada por um numero, e pode conter na sequencia uma letra,
       * o que significa que está na primeira frase do salmo.
       */
      splitSalmo = splitSalmo[1].split("\\d+(\\w)*<");

      /**
       * Dado o trecho capturado que vai do titulo "Salmo responsorial" até a primeira frase do salmo,
       * remove todas as tags HTML,
       * remove as tabulações,
       * remove espaços em branco,
       * remove astericos "*"
       */
      String resposta = splitSalmo[0]
              .replaceAll("<.*?>", " ")
              .replace("\t", "")
              .replace("&nbsp;", "")
              .replace("*", "");

      /**
       * Aplica a seguinte expressao regular com o objetivo de procurar a resposta do salmo no trecho encontrado:
       *
       * @pattern (? i)R\.\s+((?:o|ó|Ó|a|á|Á|os|as|um|uma|uns|e|de|do|da|dos|das|ao|à|À|às|Às|com|por|em|no|na|nos|nas|se|que|é|É)?\s+(?!\d)\p{L}{2}(?![.-])\p{L}+.*?$)
       *
       * (?i)         - Ativa a flag "case-insensitive" (ignora diferenças entre maiúsculas e minúsculas).
       * R\.          - Captura o literal "R.".
       * \s+          - Captura um ou mais espaços após o "R.".
       *
       * ((?: ... )?) - Grupo não capturante e opcional, que captura as palavras com 2 ou 3 letras, listadas abaixo:
       *  o|ó|Ó|a|á|Á|os|as|um|uma|uns|e|de|do|da|dos|das|ao|à|À|às|Às|com|por|em|no|na|nos|nas|se|que|é|É
       *
       * \s+          - Captura um ou mais espaços após o grupo de artigos/preposições/conjunções (se houver).
       *
       * (?!\d)       - Lookahead negativo: Garante que a palavra que segue não começará com um número.
       * \p{L}{2}     - Captura uma palavra que começa com no mínimo duas letras alfabéticas (Unicode).
       *
       * (?![.-])     - Lookahead negativo: Garante que o terceiro caractere da palavra não seja um ponto (.) ou hífen (-).
       *
       * \p{L}+       - Captura o restante da palavra, que deve ser composta por letras alfabéticas.
       * .*?          - Captura o restante da frase, até o final da linha.
       *
       * $            - Fim da linha, garantindo que a captura pare no final da linha mais próxima.
       */
      Matcher matcher = Pattern.compile("(?i)R\\.\\s+((?:o|ó|Ó|a|á|Á|os|as|um|uma|uns|e|de|do|da|dos|das|ao|à|À|às|Às|com|por|em|no|na|nos|nas|se|que|é|É)?\\s+(?!\\d)\\p{L}{2}(?![.-])\\p{L}+.*?$)", Pattern.DOTALL).matcher(resposta);

      if (matcher.find()) {
        /**
         * Se encontrar o regex, achou a resposta e com isso:
         * Verifica se no trecho da resposta existe mais de uma opção, separada com a palavra "Ou",
         * se existir retorna a resposta principal.
         * Remove o prefixo "R. " no inicio da resposta.
         * Substitui espaços com mais de um caractere "  " por apenas um caractere " ".
         */
        resposta = matcher.group()
                .split("\\b[oO]u:.*")[0]
                .replaceAll("^R\\.\\s", "")
                .replaceAll("\\s+", " ")
                .trim();
      } else {
        resposta = "";
      }

      return resposta;
    } catch (Exception ex) {
      System.out.println(ex);
      return "";
    }
  }

}
