package sylllys.insideout.controllers;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sylllys.insideout.entities.pojo.SayHello;
import sylllys.insideout.entities.pojo.ShellCommand;
import sylllys.insideout.factories.ShellFactory;

@RestController
@RequestMapping("/insideout")
public class InsideOutController {

  private static final Logger logger = LogManager.getLogger(InsideOutController.class);

  @Autowired
  ShellFactory shellFactory;

  @GetMapping("/sayhello")
  public SayHello sayhello() {

    logger.info("Received request to say hello");
    return new SayHello();

  }

  @PostMapping("/shell")
  public List<ShellCommand> shell(@RequestBody List<ShellCommand> shellCommands) {

    logger.info("Received request to execute shell command");

    for (ShellCommand command : shellCommands) {

      shellFactory.executeCommand(command);

      if (command.getExitCode() != 0) {
        break;
      }
    }

    logger.info("Completed request to execute shell command");
    return shellCommands;
  }
}
