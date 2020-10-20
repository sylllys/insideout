package sylllys.insideout.factories;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import sylllys.insideout.entities.pojo.ShellCommand;

public class ShellFactory {

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

  public void executeCommand(ShellCommand command) {

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
