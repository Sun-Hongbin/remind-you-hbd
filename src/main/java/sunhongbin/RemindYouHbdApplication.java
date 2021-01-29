package sunhongbin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RemindYouHbdApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext appCtx = SpringApplication.run(RemindYouHbdApplication.class, args);

//		WebController instance = appCtx.getBean(WebController.class);
//
//		instance.doAiMan();
	}
}
