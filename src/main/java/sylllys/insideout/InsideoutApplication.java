package sylllys.insideout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy
public class InsideoutApplication {

	public static void main(String[] args) {
		SpringApplication.run(InsideoutApplication.class, args);
	}

}
