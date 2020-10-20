package sylllys.insideout.entities.pojo;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;

public class ShellCommand {

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  String command = new String();

  public String getOutput() {
    return output;
  }

  public void setOutput(String output) {
    this.output = output;
  }

  String output = null;


  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  String error = null;

  public Integer getExitCode() {
    return exitCode;
  }

  public void setExitCode(Integer exitCode) {
    this.exitCode = exitCode;
  }

  Integer exitCode = null;
}
