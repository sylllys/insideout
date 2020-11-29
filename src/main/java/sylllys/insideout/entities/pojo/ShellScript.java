package sylllys.insideout.entities.pojo;

import java.util.ArrayList;

public class ShellScript {

  public String getScriptPath() {
    return scriptPath;
  }

  public void setScriptPath(String scriptPath) {
    this.scriptPath = scriptPath;
  }

  String scriptPath = new String();

  public ArrayList<String> getArgs() {
    return args;
  }

  public void setArgs(ArrayList<String> args) {
    this.args = args;
  }

  ArrayList<String> args = new ArrayList<String>();

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
