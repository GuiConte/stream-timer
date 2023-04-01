package streamtimer.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import org.springframework.stereotype.Component;
import streamtimer.model.LiturgiaDiaria;

@Component
public class Aleluia {

  private static final String CURRENT_PATH = System.getProperty("user.dir");
  private static final String ALELUIA_PROPERTIES_NAME = "aleluia.properties";
  private static final String ALELUIA_PROPERTIES_PATH = CURRENT_PATH + File.separator + ALELUIA_PROPERTIES_NAME;
  private static final String LEITURAS_PROPERTIES_NAME = "textos.properties";
  private static final String LEITURAS_PROPERTIES_PATH = CURRENT_PATH + File.separator + LEITURAS_PROPERTIES_NAME;

  private static final String LEITURA_1 = "Leitura I (";
  private static final String SALMO = "Salmo Responsorial ";

  private JFrame window;
  private JPanel panelLiturgy;
  private JList listLiturgy;
  private JButton btnNext;
  private String[] leituras, salmos, respostas;
  private LiturgiaDiaria liturgiaDiaria;

  public void draw(){

    if (getLiturgy()) {
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
          liturgiaDiaria.setLeitura1(LEITURA_1.concat(leituras[index]).concat(")"));
          liturgiaDiaria.setSalmo(SALMO.concat(salmos[index]));
          liturgiaDiaria.setSalmoResposta(respostas[index]);
        } else {
          listLiturgy.setSelectedIndex(0);
          liturgiaDiaria.setLeitura1(LEITURA_1.concat(leituras[0]).concat(")"));
          liturgiaDiaria.setSalmo(SALMO.concat(salmos[0]));
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

  private Boolean getLiturgy() {
    try {
      FileInputStream file = new FileInputStream(ALELUIA_PROPERTIES_PATH);
      Properties props = new Properties();
      props.load(new InputStreamReader(file, StandardCharsets.UTF_8));
      leituras = props.getProperty("leituras").split(";");
      salmos = props.getProperty("salmos").split(";");
      respostas = props.getProperty("respostas").split(";");
      liturgiaDiaria = new LiturgiaDiaria();
      liturgiaDiaria.setLeitura1(LEITURA_1.concat(leituras[0]).concat(")"));
      liturgiaDiaria.setSalmo(SALMO.concat(salmos[0]));
      liturgiaDiaria.setSalmoResposta(respostas[0]);
      updateLiturgy(liturgiaDiaria);
      return true;
    } catch (Exception ex){
      return false;
    }
  }

}
