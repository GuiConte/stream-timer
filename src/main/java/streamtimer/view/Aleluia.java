package streamtimer.view;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import org.springframework.stereotype.Component;
import streamtimer.model.LiturgiaAPIv2;
import streamtimer.model.LiturgiaDiaria;
import streamtimer.service.LiturgiaDiariaServiceLiturgiaAPIv2Impl;

@Component
public class Aleluia {

  private static final String CURRENT_PATH = System.getProperty("user.dir");
  private static final String ALELUIA_PROPERTIES_NAME = "aleluia.properties";
  private static final String ALELUIA_PROPERTIES_PATH = CURRENT_PATH + File.separator + ALELUIA_PROPERTIES_NAME;
  private static final String LEITURAS_PROPERTIES_NAME = "textos.properties";
  private static final String LEITURAS_PROPERTIES_PATH = CURRENT_PATH + File.separator + LEITURAS_PROPERTIES_NAME;

  private static final String LEITURA = "Leitura (";
  private static final String SALMO = "Salmo Responsorial (";

  private JFrame window;
  private JPanel panelLiturgy;
  private JList listLiturgy;
  private JButton btnNext;
  private String[] leituras, salmos, respostas;
  private LiturgiaDiaria liturgiaDiaria;

  public void draw(LocalDate liturgyDate){

    if (getLiturgy(liturgyDate)) {
      window = new JFrame("Stream Timer v2");
      window.setSize(200, 250);
      window.setLayout(null);
      window.setResizable(false);
      window.setLocationRelativeTo(null);

      panelLiturgy = new JPanel();
      panelLiturgy.setLayout(null);
      panelLiturgy.setBounds(0, 2, 200, 250);

      listLiturgy = new JList(leituras);
      listLiturgy.setVisible(true);
      listLiturgy.setBounds(0, 2, 200, 140);
      listLiturgy.setSelectedIndex(0);
      listLiturgy.setEnabled(false);

      btnNext = new JButton("Proximo");
      btnNext.setBounds(20, 160, 155, 30);
      btnNext.addActionListener((java.awt.event.ActionEvent evt) -> {
        listLiturgy.setEnabled(true);
        Integer index = listLiturgy.getSelectedIndex() + 1;
        listLiturgy.setSelectedIndex(index);
        if (index < leituras.length){
          String leitura = index < leituras.length ? LEITURA.concat(leituras[index]).concat(")") : "";
          String salmo = index < salmos.length ? SALMO.concat(salmos[index]).concat(")") : "";;
          String resposta = index < respostas.length ? respostas[index] : "";;
          liturgiaDiaria.setLeitura1(leitura);
          liturgiaDiaria.setSalmo(salmo);
          liturgiaDiaria.setSalmoResposta(resposta);
        } else {
          listLiturgy.setSelectedIndex(0);
          liturgiaDiaria.setLeitura1(LEITURA.concat(leituras[0]).concat(")"));
          liturgiaDiaria.setSalmo(SALMO.concat(salmos[0]).concat(")"));
          liturgiaDiaria.setSalmoResposta(respostas[0]);
        }
        updateLiturgy(liturgiaDiaria);
        listLiturgy.setEnabled(false);
      });
      panelLiturgy.add(btnNext);

      panelLiturgy.add(listLiturgy);

      window.add(panelLiturgy);
      window.setVisible(true);

    }

  }

  private void updateLiturgy(LiturgiaDiaria liturgiaDiaria) {
    try {
      FileInputStream file = new FileInputStream(LEITURAS_PROPERTIES_PATH);
      Properties props = new Properties();
      props.load(file);

      Files.write(Paths.get(props.getProperty("leitura1.path")),
          liturgiaDiaria.getLeitura1().getBytes(StandardCharsets.UTF_8));
      Files.write(Paths.get(props.getProperty("salmo.path")),
          liturgiaDiaria.getSalmo().getBytes(StandardCharsets.UTF_8));
      Files.write(Paths.get(props.getProperty("salmo_resposta.path")),
          liturgiaDiaria.getSalmoResposta().getBytes(StandardCharsets.UTF_8));
    } catch (IOException ex){
      ex.printStackTrace();
    }
  }

  private Boolean getLiturgy(LocalDate liturgyDate) {
    try {
      Properties props = loadPropertiesFile();
      props = fillPropertiesWithExternalAPIIfEmpty(props, liturgyDate);
      leituras = props.getProperty("leituras").split(";");
      salmos = props.getProperty("salmos").split(";");
      respostas = props.getProperty("respostas").split(";");
      liturgiaDiaria = new LiturgiaDiaria();
      liturgiaDiaria.setLeitura1(LEITURA.concat(leituras[0]).concat(")"));
      liturgiaDiaria.setSalmo(SALMO.concat(salmos[0]).concat(")"));
      liturgiaDiaria.setSalmoResposta(respostas[0]);
      updateLiturgy(liturgiaDiaria);
      return true;
    } catch (Exception ex){
      return false;
    }
  }

  private Properties fillPropertiesWithExternalAPIIfEmpty(Properties props, LocalDate liturgyDate) throws Exception {
    if (props.getProperty("leituras").isEmpty()
        && props.getProperty("salmos").isEmpty()
        && props.getProperty("respostas").isEmpty()) {
      String date = liturgyDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
      LiturgiaAPIv2 liturgy = new LiturgiaDiariaServiceLiturgiaAPIv2Impl().getLiturgyFromExternalAPI(date);

      String propertyFileContent = "leituras=".concat(liturgy.getLeituras().getAleluiaLeituras())
                                    .concat("\nsalmos=").concat(liturgy.getLeituras().getAleluiaSalmos())
                                    .concat("\nrespostas=").concat(liturgy.getLeituras().getAleluiaSalmosRespostas());

      Files.write(Paths.get(ALELUIA_PROPERTIES_PATH),
              propertyFileContent.getBytes(StandardCharsets.UTF_8));

      return loadPropertiesFile();
    }

    return props;
  }

  private Properties loadPropertiesFile() throws Exception {
    FileInputStream file = new FileInputStream(ALELUIA_PROPERTIES_PATH);
    Properties props = new Properties();
    props.load(new InputStreamReader(file, StandardCharsets.UTF_8));

    return props;
  }

}
