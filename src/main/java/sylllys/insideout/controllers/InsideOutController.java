package sylllys.insideout.controllers;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sylllys.insideout.entities.pojo.SayHello;
import sylllys.insideout.entities.pojo.ShellCommand;
import sylllys.insideout.factories.HTTPFactory;
import sylllys.insideout.factories.ShellFactory;

@RestController
@RequestMapping("/")
public class InsideOutController {

  private static final Logger logger = LogManager.getLogger(InsideOutController.class);

  @GetMapping("/sayhello")
  public SayHello sayhello() {

    logger.info("Received request to say hello");
    return new SayHello();

  }

  @RequestMapping("/http")
  public void http(HttpServletRequest request, HttpServletResponse response,
      @RequestHeader("insideout-redirect-url") String redirectURL) {

    logger.info("Received request to redirect url:" + redirectURL);

    HTTPFactory httpFactory = new HTTPFactory(request, response, redirectURL);
    httpFactory.redirectRequest();

    logger.info("Completed request for redirect url:" + redirectURL);
  }

  @PostMapping("/shell")
  public List<ShellCommand> shell(@RequestBody List<ShellCommand> shellCommands) {

    logger.info("Received request to execute shell command");

    for (ShellCommand command : shellCommands) {

      new ShellFactory().executeCommand(command);

      if (command.getExitCode() != 0) {
        break;
      }
    }

    logger.info("Completed request to execute shell command");
    return shellCommands;
  }
}
