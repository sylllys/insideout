package sylllys.insideout.factories;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import sylllys.insideout.controllers.InsideOutController;
import sylllys.insideout.entities.pojo.ShellCommand;
import sylllys.insideout.entities.pojo.ShellScript;
import sylllys.insideout.properties.ShellProperties;

@Component
public class ShellFactory {

  private static final Logger logger = LogManager.getLogger(ShellFactory.class);

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

  private boolean isShellScriptAllowed(String requestedShellScript) {

    if (shellProperties.getAllowedScripts() == null) {
      return false;
    }

    for (String allowedShellScript : shellProperties.getAllowedScripts().split(",")) {
      if (requestedShellScript.equalsIgnoreCase(allowedShellScript.trim())) {
        return true;
      }
    }

    return false;
  }

  private boolean isChainedCommand(String requestedShellCommandLine) {

    final String regex = "(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)";
    final String chainOperators = "&&,&,\\|\\|,\\|,!,>,>>,;";

    for (String chainOperator : chainOperators.split(",")) {

      Pattern p = Pattern.compile(chainOperator + regex);
      Matcher m = p.matcher(requestedShellCommandLine);
      if (m.find()) {
        return false;
      }
    }

    if (requestedShellCommandLine.trim().endsWith("\\")) {
      return false;
    }

    return true;
  }

  private boolean areDoubleQuotesBalanced(String text) {

    int count = StringUtils.countMatches(text, "\"");

    return count % 2 == 0;
  }

  public void executeCommand(ShellCommand command) {

    if (!areDoubleQuotesBalanced(command.getCommand())) {
      command.setError("Double quotes are not balanced.");
      command.setExitCode(3);
      return;
    }

    if (!isChainedCommand(command.getCommand())) {
      command.setError("Chained commands are not supported.");
      command.setExitCode(3);
      return;
    }

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
      logger.error(e.getStackTrace());
      command.setError("Unhandled error when tried to execute this request");
    }
  }

  public void executeShellScript(ShellScript shellScript) {

    if (!isShellScriptAllowed(shellScript.getScriptPath())) {
      shellScript
          .setError("This script path is not allowed, please add this to insideout allowed list.");
      shellScript.setExitCode(3);
      return;
    }

    if (!new File(shellScript.getScriptPath()).exists()) {
      shellScript
          .setError("No script exists with this path");
      shellScript.setExitCode(3);
      return;
    }

    try {

      ArrayList<String> command = new ArrayList<String>();
      command.add(shellScript.getScriptPath());
      command.addAll(shellScript.getArgs());

      ProcessBuilder builder = new ProcessBuilder(command);
      Process process = builder.start();

      shellScript.setOutput(captureCommandOutput(process));
      shellScript.setError(captureCommandError(process));
      shellScript.setExitCode(process.waitFor());

    } catch (Exception e) {
      logger.error(e.getStackTrace());
      shellScript.setError("Unhandled error when tried to execute this request");
    }
  }

}
