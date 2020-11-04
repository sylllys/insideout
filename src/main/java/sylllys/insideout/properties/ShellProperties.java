package sylllys.insideout.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("insideout.shell")
public class ShellProperties {

  public String getAllowedCommands() {
    return allowedCommands;
  }

  public void setAllowedCommands(String allowedCommands) {
    this.allowedCommands = allowedCommands;
  }

  private String allowedCommands;
}
