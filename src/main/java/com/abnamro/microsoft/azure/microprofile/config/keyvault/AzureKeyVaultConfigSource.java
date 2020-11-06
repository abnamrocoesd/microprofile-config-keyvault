package com.abnamro.microsoft.azure.microprofile.config.keyvault;

import java.util.Collections;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.Map;
import java.util.Set;

public class AzureKeyVaultConfigSource implements ConfigSource {

  public static final String FALSE = "false";
  private AzureKeyVaultOperation keyVaultOperation;

  // true by default
  private boolean isKeyVaultEnabled = true;


  public AzureKeyVaultConfigSource() {
    // no op
  }

  private void init() {
    if (keyVaultOperation != null) {
      return;
    }

    Config config = ConfigProvider.getConfig();
    this.isKeyVaultEnabled =
        config
            .getOptionalValue("azure.keyvault.enabled", Boolean.class)
            .orElse(Boolean.TRUE);

    if (!isKeyVaultEnabled) {
      return;
    }

    keyVaultOperation = new AzureKeyVaultOperation();
  }


  @Override
  public Map<String, String> getProperties() {
    init();
    return isKeyVaultEnabled ? keyVaultOperation.getProperties() : Collections.emptyMap();
  }

  @Override
  public String getValue(String key) {
    init();
    return isKeyVaultEnabled ? keyVaultOperation.getValue(key) : null;
  }

  @Override
  public String getName() {
    return "AzureKeyVaultConfigSource";
  }

  @Override
  public Set<String> getPropertyNames() {
    init();
    return isKeyVaultEnabled ? keyVaultOperation.getKeys() : Collections.emptySet();
  }

  @Override
  public int getOrdinal() {
    return 90;
  }
}
