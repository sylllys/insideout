package sylllys.insideout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import sylllys.insideout.filters.SimpleFilterConfiguration;

@SpringBootApplication
@EnableZuulProxy
//uncomment this code if you want to component scan any packages that are not under sylllys.insideout
//@ComponentScan(basePackages = "package.path")
public class InsideoutApplication {

  public static void main(String[] args) {
    SpringApplication.run(InsideoutApplication.class, args);
  }

  @Bean
  public SimpleFilterConfiguration simpleFilter() {
    return new SimpleFilterConfiguration();
  }

}
