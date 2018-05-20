package messenger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class MessengerApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(MessengerApplication.class, args);
        // TestBD testBD = context.getBean(TestBD.class);
        // testBD.test();
    }
}
