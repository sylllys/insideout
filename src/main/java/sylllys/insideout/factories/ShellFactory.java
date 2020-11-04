package sylllys.insideout.factories;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import sylllys.insideout.entities.pojo.ShellCommand;
import sylllys.insideout.properties.ShellProperties;

@Component
public class ShellFactory {

  @Autowired
  ShellProperties shellProperties;

  public boolean isThisWindowsOS() {

    return System.getProperty("os.name").toLowerCase().startsWith("windows");
  }

  public String getTerminalProgram() {
    return isThisWindowsOS() ? "cmd.exe" : "sh";
  }

  public String getTerminalInterpreter() {
    return isThisWindowsOS() ? "/c" : "-c";
  }

  private String captureCommandOutput(Process process) throws IOException {

    return readBufferedReader(new BufferedReader(new InputStreamReader(process.getInputStream())));
  }

  private String captureCommandError(Process process) throws IOException {

    return readBufferedReader(new BufferedReader(new InputStreamReader(process.getErrorStream())));
  }

  private String readBufferedReader(BufferedReader stdInput) throws IOException {

    String s = null;
    String output = "";
    while ((s = stdInput.readLine()) != null) {
      output = output + s;
    }

    return output;
  }

  private boolean isShellCommandAllowed(String requestedShellCommand) {

    if (shellProperties.getAllowedCommands() == null) {
      return false;
    }

    for (String allowedShellCommand : shellProperties.getAllowedCommands().split(",")) {
      if (requestedShellCommand.equalsIgnoreCase(allowedShellCommand.trim())) {
        return true;
      }
    }

    return false;
  }

  public void executeCommand(ShellCommand command) {

    if (!isShellCommandAllowed(command.getCommand().split(" ", 2)[0])) {
      command.setError("This command is not allowed, please add this to insideout allowed list.");
      command.setExitCode(3);
      return;
    }

    try {

      ProcessBuilder builder = new ProcessBuilder();
      builder.command(getTerminalProgram(), getTerminalInterpreter(), command.getCommand());
      Process process = builder.start();

      command.setOutput(captureCommandOutput(process));
      command.setError(captureCommandError(process));
      command.setExitCode(process.waitFor());

    } catch (Exception e) {
      command.setOutput(e.getStackTrace().toString());
    }
  }

}
