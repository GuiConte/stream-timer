package streamtimer.view;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DateEditor;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import org.springframework.beans.factory.annotation.Autowired;
import streamtimer.model.LiturgiaDiaria;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.stereotype.Component;
import streamtimer.service.LiturgiaDiariaService;
import streamtimer.service.LiturgiaDiariaServiceLiturgiaAPIv2Impl;

@Component
public class StreamTimer {

  private static final String ERROR_MSG = "Horario atual nao pode ser superior ao horario de inicio menos o tempo de abertura";
  private static final String ERROR_INTEG_MSG = "Houve um erro e o processo não foi concluido, refaça o processo manualmente";
  private static final String TIME_LEFT = "Tempo:   ";
  private static final String CURRENT_PATH = System.getProperty("user.dir");
  private static final String FILE_NAME = "timer.txt";
  private static final String PROPERTIES_NAME = "textos.properties";
  private static final String ABSOLUTE_PATH = CURRENT_PATH + File.separator + FILE_NAME;
  private static final String PROPERTIES_PATH = CURRENT_PATH + File.separator + PROPERTIES_NAME;
  private static final int MARGIN_ERROR = 1;

  private JFrame window;
  private JPanel panelTimer, panelLiturgy;
  private JLabel lblStartTime, lblOpeningVideoTime, lblTimeLeft;
  private JLabel lblLiturgyDate, lblText1, lblText2, lblText3, lblText4, lblText5;
  private JSpinner spnStartTime, spnLiturgyDate;
  private JTextField txtOpeningVideoTime;
  private JTextField txtText1, txtText2, txtText3, txtText4;
  private JTextArea txtText5;
  private JScrollPane scrollText5;
  private JButton btnStartTimer, btnUpdateTexts, btnAleluia;

  private Thread thread;
  private LiturgiaDiariaService liturgiaDiariaService = new LiturgiaDiariaServiceLiturgiaAPIv2Impl();

  @Autowired
  private Aleluia aleluia;

  public void drawWindow() throws Exception {
    window = new JFrame("Stream Timer v2");
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setSize(500, 300);
    window.setLayout(null);
    window.setResizable(false);
    window.setLocationRelativeTo(null);
    window.setIconImage(new ImageIcon(getClass().getResource("/timer.png")).getImage());

    panelTimer = new JPanel();
    panelTimer.setLayout(null);
    panelTimer.setBounds(0, 2, 220, 300);

    lblStartTime = new JLabel("Horario Inicio: ");
    lblStartTime.setBounds(10, 12, 100, 22);
    panelTimer.add(lblStartTime);

    Date startTimeDefault = Date.from(
        LocalDateTime.of(LocalDate.now(), LocalTime.of(19, 0))
            .atZone(ZoneId.systemDefault())
            .toInstant()
    );
    SpinnerDateModel spinnerDateModel = new SpinnerDateModel(startTimeDefault, null, null,
        Calendar.HOUR_OF_DAY);
    spnStartTime = new JSpinner(spinnerDateModel);
    DateEditor dateEditor = new DateEditor(spnStartTime, "HH:mm");
    dateEditor.getTextField().setHorizontalAlignment(SwingConstants.RIGHT);
    spnStartTime.setEditor(dateEditor);
    spnStartTime.setBounds(140, 10, 60, 30);
    panelTimer.add(spnStartTime);

    lblOpeningVideoTime = new JLabel("Abertura (segundos):");
    lblOpeningVideoTime.setBounds(10, 50, 120, 22);
    panelTimer.add(lblOpeningVideoTime);

    txtOpeningVideoTime = new JTextField();
    txtOpeningVideoTime.setBounds(140, 50, 60, 25);
    txtOpeningVideoTime.setText("30");
    txtOpeningVideoTime.setHorizontalAlignment(SwingConstants.RIGHT);
    panelTimer.add(txtOpeningVideoTime);

    btnStartTimer = new JButton("Iniciar");
    btnStartTimer.setBounds(30, 90, 150, 30);
    btnStartTimer.addActionListener((java.awt.event.ActionEvent evt) -> {
      Duration timeLeft = calculateTimeLeft(0);
      if (!timeLeft.isNegative()) {
        startThread();
      } else {
        JOptionPane.showMessageDialog(null, ERROR_MSG, "Erro", JOptionPane.ERROR_MESSAGE);
      }
    });
    panelTimer.add(btnStartTimer);

    lblTimeLeft = new JLabel(TIME_LEFT);
    lblTimeLeft.setBounds(20, 130, 120, 22);
    panelTimer.add(lblTimeLeft);

    btnAleluia = new JButton("Sabado Aleluia");
    btnAleluia.setBounds(30, 220, 150, 30);
    btnAleluia.addActionListener((java.awt.event.ActionEvent evt) -> {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime((Date) spnLiturgyDate.getValue());
      LocalDate dataLiturgia = LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId()).toLocalDate();
      aleluia.draw(dataLiturgia);
    });
    panelTimer.add(btnAleluia);

    /* ********************************************************************************************* */

    panelLiturgy = new JPanel();
    panelLiturgy.setLayout(null);
    panelLiturgy.setBackground(Color.lightGray);
    panelLiturgy.setBounds(220, 0, 275, 300);

    lblLiturgyDate = new JLabel("Data Liturgia: ");
    lblLiturgyDate.setBounds(40, 12, 100, 22);
    panelLiturgy.add(lblLiturgyDate);

    SpinnerDateModel spinnerDateModel2 = new SpinnerDateModel(startTimeDefault, null, null,
        Calendar.DATE);
    spnLiturgyDate = new JSpinner(spinnerDateModel2);
    DateEditor dateEditor2 = new DateEditor(spnLiturgyDate, "dd/MM/yyyy");
    dateEditor.getTextField().setHorizontalAlignment(SwingConstants.RIGHT);
    spnLiturgyDate.setEditor(dateEditor2);
    spnLiturgyDate.setBounds(170, 10, 90, 30);
    panelLiturgy.add(spnLiturgyDate);

    btnUpdateTexts = new JButton("Atualizar Textos");
    btnUpdateTexts.setBounds(65, 50, 150, 30);
    btnUpdateTexts.addActionListener((java.awt.event.ActionEvent evt) -> {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime((Date) spnLiturgyDate.getValue());
      LocalDate dataLiturgia = LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId()).toLocalDate();
      LiturgiaDiaria liturgiaDiaria = liturgiaDiariaService.getLiturgiaDiaria(dataLiturgia);
      if (!liturgiaDiaria.getError()) {
        try {
          updateLiturgy(liturgiaDiaria);
        } catch (IOException e) {
          e.printStackTrace();
          JOptionPane.showMessageDialog(null, ERROR_INTEG_MSG, "Erro", JOptionPane.ERROR_MESSAGE);
        }
      } else {
        JOptionPane.showMessageDialog(null, ERROR_INTEG_MSG, "Erro", JOptionPane.ERROR_MESSAGE);
      }
    });
    panelLiturgy.add(btnUpdateTexts);

    lblText1 = new JLabel("Leitura I:");
    lblText1.setBounds(10, 90, 120, 22);
    panelLiturgy.add(lblText1);

    txtText1 = new JTextField();
    txtText1.setBounds(75, 90, 185, 22);
    txtText1.setEditable(false);
    txtText1.setHorizontalAlignment(SwingConstants.LEFT);
    panelLiturgy.add(txtText1);

    lblText2 = new JLabel("Leitura II:");
    lblText2.setBounds(10, 120, 120, 22);
    panelLiturgy.add(lblText2);

    txtText2 = new JTextField();
    txtText2.setBounds(75, 120, 185, 22);
    txtText2.setEditable(false);
    txtText2.setHorizontalAlignment(SwingConstants.LEFT);
    panelLiturgy.add(txtText2);

    lblText3 = new JLabel("Evangelho:");
    lblText3.setBounds(10, 150, 120, 22);
    panelLiturgy.add(lblText3);

    txtText3 = new JTextField();
    txtText3.setBounds(75, 150, 185, 22);
    txtText3.setEditable(false);
    txtText3.setHorizontalAlignment(SwingConstants.LEFT);
    panelLiturgy.add(txtText3);

    lblText4 = new JLabel("Salmo:");
    lblText4.setBounds(10, 180, 120, 22);
    panelLiturgy.add(lblText4);

    txtText4 = new JTextField();
    txtText4.setBounds(75, 180, 185, 22);
    txtText4.setEditable(false);
    txtText4.setHorizontalAlignment(SwingConstants.LEFT);
    panelLiturgy.add(txtText4);

    lblText5 = new JLabel("Resposta:");
    lblText5.setBounds(10, 210, 120, 22);
    panelLiturgy.add(lblText5);

    txtText5 = new JTextArea();
    txtText5.setBounds(75, 210, 185, 50);
    txtText5.setEditable(false);

    scrollText5 = new JScrollPane(txtText5);
    scrollText5.setBounds(75, 210, 185, 50);
    panelLiturgy.add(scrollText5);

    window.add(panelTimer);
    window.add(panelLiturgy);
    window.setVisible(true);
  }

  private LocalTime convertDateToLocalTime(Date time) {
    return time.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
  }

  private void startThread() {
    thread = new Thread(() -> {
      Duration timeLeft = null;
      do{
        try {
          Thread.sleep(1000);
          timeLeft = calculateTimeLeft(1);
          exportToFile(durationToString(timeLeft));
          lblTimeLeft.setText(TIME_LEFT + durationToString(timeLeft));
        } catch (InterruptedException e) {
          thread.interrupt();
        } catch (IOException e) {
          thread.interrupt();
        }
      }while (!timeLeft.isZero() && !timeLeft.isNegative());
      thread.interrupt();
    });

    thread.start();
  }

  private Duration calculateTimeLeft(int minusSeconds){
    LocalTime transmissionStart = convertDateToLocalTime((Date) spnStartTime.getValue());
    LocalTime now = LocalTime.now();
    int openingVideoTime = Integer.parseInt(txtOpeningVideoTime.getText())-MARGIN_ERROR;

    return Duration.between(now, transmissionStart).minusSeconds(openingVideoTime).minusSeconds(minusSeconds);
  }

  private String durationToString(Duration timeLeft){
    if (!timeLeft.isNegative())
      return DurationFormatUtils.formatDuration(timeLeft.toMillis(), "mm:ss", true);
    else
      return "";
  }

  private void exportToFile(String timeLeft) throws IOException {
    Files.write(Paths.get(ABSOLUTE_PATH), timeLeft.getBytes(StandardCharsets.UTF_8));
  }

  private void updateLiturgy(LiturgiaDiaria liturgiaDiaria) throws IOException {
    FileInputStream file = new FileInputStream(PROPERTIES_PATH);
    Properties props = new Properties();
    props.load(file);

    if (liturgiaDiaria.getLeitura1() != null) {
      Files.write(Paths.get(props.getProperty("leitura1.path")),
          liturgiaDiaria.getLeitura1().getBytes(StandardCharsets.UTF_8));
      txtText1.setText(liturgiaDiaria.getLeitura1());
    } else {
      txtText1.setText("");
    }

    if (liturgiaDiaria.getLeitura2() != null) {
      Files.write(Paths.get(props.getProperty("leitura2.path")),
          liturgiaDiaria.getLeitura2().getBytes(StandardCharsets.UTF_8));
      txtText2.setText(liturgiaDiaria.getLeitura2());
    } else {
      txtText2.setText("");
    }

    if (liturgiaDiaria.getEvangelho() != null) {
      Files.write(Paths.get(props.getProperty("evangelho.path")),
          liturgiaDiaria.getEvangelho().getBytes(StandardCharsets.UTF_8));
      txtText3.setText(liturgiaDiaria.getEvangelho());
    } else {
      txtText3.setText("");
    }

    if (liturgiaDiaria.getSalmo() != null) {
      Files.write(Paths.get(props.getProperty("salmo.path")),
          liturgiaDiaria.getSalmo().getBytes(StandardCharsets.UTF_8));
      txtText4.setText(liturgiaDiaria.getSalmo());
    } else {
      txtText4.setText("");
    }

    if (liturgiaDiaria.getSalmoResposta() != null) {
      Files.write(Paths.get(props.getProperty("salmo_resposta.path")),
          liturgiaDiaria.getSalmoResposta().getBytes(StandardCharsets.UTF_8));
      txtText5.setText(liturgiaDiaria.getSalmoResposta());
    } else {
      txtText5.setText("");
    }
  }

}
