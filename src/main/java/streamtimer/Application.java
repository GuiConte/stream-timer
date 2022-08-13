package streamtimer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import streamtimer.view.StreamTimer;

@SpringBootApplication
public class Application implements ApplicationRunner {

  @Autowired
  private StreamTimer streamTimer;

  public static void main(String[] args) {
    new SpringApplicationBuilder(Application.class).headless(false).run(args);
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    streamTimer.drawWindow();
  }
}
