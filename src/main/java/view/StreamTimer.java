package view;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DateEditor;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class StreamTimer {

  private static final String ERROR_MSG = "Horario atual nao pode ser superior ao horario de inicio menos o tempo de abertura";
  private static final String TIME_LEFT = "Tempo:   ";
  private static final String CURRENT_PATH = System.getProperty("user.dir");
  private static final String FILE_NAME = "timer.txt";
  private static final String ABSOLUTE_PATH = CURRENT_PATH + File.separator + FILE_NAME;
  private static final int MARGIN_ERROR = 1;

  private JFrame window;
  private JPanel panel;
  private JLabel lblStartTime, lblOpeningVideoTime, lblTimeLeft;
  private JSpinner spnStartTime;
  private JTextField txtOpeningVideoTime;
  private JButton btnStartTimer;

  private Thread thread;

  public StreamTimer() throws ParseException {
    drawWindow();
  }

  public void drawWindow() throws ParseException {
    window = new JFrame("Stream Timer v2");
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setSize(220, 190);
    window.setLayout(null);
    window.setResizable(false);
    window.setLocationRelativeTo(null);
    window.setIconImage(new ImageIcon(getClass().getResource("/timer.png")).getImage());

    panel = new JPanel();
    panel.setLayout(null);
    panel.setBounds(0, 02, 220, 190);

    lblStartTime = new JLabel("Horario Inicio: ");
    lblStartTime.setBounds(10, 12, 100, 22);
    panel.add(lblStartTime);

    Date startTimeDefault = Date.from(
        LocalDateTime.of(LocalDate.now(), LocalTime.of(19, 0))
            .atZone(ZoneId.systemDefault())
            .toInstant()
    );
    SpinnerDateModel spinnerDateModel = new SpinnerDateModel(startTimeDefault, null, null, Calendar.HOUR_OF_DAY);
    spnStartTime = new JSpinner(spinnerDateModel);
    DateEditor dateEditor = new DateEditor(spnStartTime, "HH:mm");
    dateEditor.getTextField().setHorizontalAlignment(SwingConstants.RIGHT);
    spnStartTime.setEditor(dateEditor);
    spnStartTime.setBounds(140, 10, 60, 30);
    panel.add(spnStartTime);

    lblOpeningVideoTime = new JLabel("Abertura (segundos):");
    lblOpeningVideoTime.setBounds(10, 50, 120, 22);
    panel.add(lblOpeningVideoTime);

    txtOpeningVideoTime = new JTextField();
    txtOpeningVideoTime.setBounds(140, 50, 60, 25);
    txtOpeningVideoTime.setText("30");
    txtOpeningVideoTime.setHorizontalAlignment(SwingConstants.RIGHT);
    panel.add(txtOpeningVideoTime);

    btnStartTimer = new JButton("Iniciar");
    btnStartTimer.setBounds(window.getWidth() / 4, 90, (window.getWidth() / 4) * 2, 30);
    btnStartTimer.addActionListener((java.awt.event.ActionEvent evt) -> {
      Duration timeLeft = calculateTimeLeft(0);
      if (!timeLeft.isNegative()) {
        startThread();
      } else {
        JOptionPane.showMessageDialog(null, ERROR_MSG, "Erro", JOptionPane.ERROR_MESSAGE);
      }
    });
    panel.add(btnStartTimer);

    lblTimeLeft = new JLabel(TIME_LEFT);
    lblTimeLeft.setBounds(20, 130, 120, 22);
    panel.add(lblTimeLeft);

    window.add(panel);
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

}
