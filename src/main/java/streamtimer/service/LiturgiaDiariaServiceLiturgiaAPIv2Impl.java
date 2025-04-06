package streamtimer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import streamtimer.model.LiturgiaAPIv2;
import streamtimer.model.LiturgiaDiaria;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.util.Objects.nonNull;

@Slf4j
public class LiturgiaDiariaServiceLiturgiaAPIv2Impl implements LiturgiaDiariaService {

    private static final String LEITURA_1 = "Leitura I (";
    private static final String LEITURA_2 = "Leitura II (";
    private static final String SALMO = "Salmo Responsorial (";
    private static final String EVANGELHO = "Evangelho (";

    private static final String URL_BASE = "https://liturgia.up.railway.app/v2/";

    @Override
    public LiturgiaDiaria getLiturgiaDiaria(LocalDate dataLiturgia) {
        String data = dataLiturgia.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LiturgiaDiaria liturgiaDiaria = new LiturgiaDiaria();

        LiturgiaAPIv2 liturgia = getLiturgyFromExternalAPI(data);
        if (nonNull(liturgia)) {
            liturgiaDiaria.setLeitura1(LEITURA_1.concat(liturgia.getLeituras().getPrimeiraLeitura()).concat(")"));
            liturgiaDiaria.setLeitura2(LEITURA_2.concat(liturgia.getLeituras().getSegundaLeitura()).concat(")"));
            liturgiaDiaria.setSalmo(SALMO.concat(liturgia.getLeituras().getSalmo()).concat(")"));
            liturgiaDiaria.setSalmoResposta(liturgia.getLeituras().getSalmoResposta());
            liturgiaDiaria.setEvangelho(EVANGELHO.concat(liturgia.getLeituras().getEvangelho()).concat(")"));
        } else {
            liturgiaDiaria.setError(true);
        }

        return liturgiaDiaria;
    }

    public LiturgiaAPIv2 getLiturgyFromExternalAPI(String date){
        RestTemplate restTemplate = new RestTemplate();
        try {
            LiturgiaAPIv2 liturgia = restTemplate.getForObject(URL_BASE+date, LiturgiaAPIv2.class);
            return liturgia;
        } catch (Exception ex){
            log.error(ex.getMessage());
            return null;
        }
    }

}