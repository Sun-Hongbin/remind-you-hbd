package sunhongbin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import sunhongbin.controller.WebController;
import sunhongbin.service.WeChatService;

@SpringBootApplication
public class RemindYouHbdApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext appCtx = SpringApplication.run(RemindYouHbdApplication.class, args);

//		WebController instance = appCtx.getBean(WebController.class);
//
//		instance.doAiMan();
	}
}
