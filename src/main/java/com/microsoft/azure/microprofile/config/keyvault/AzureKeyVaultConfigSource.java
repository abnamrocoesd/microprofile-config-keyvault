package com.microsoft.azure.microprofile.config.keyvault;

import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import java.util.HashMap;
import java.util.HashSet;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.Map;
import java.util.Set;

public class AzureKeyVaultConfigSource implements ConfigSource {

  public static final String FALSE = "false";
  private AzureKeyVaultOperation keyVaultOperation;

  private String configIsKeyVaultEnabled;

  public AzureKeyVaultConfigSource() {
    // no-op
  }

  private void init() {
    if (keyVaultOperation != null) {
      return;
    }
    Config config = ConfigProvider.getConfig();

    if (isKeyVaultDisabled(config)) {
      return;
    }

    // read in keyvault config settings from external config source (normally the environment or a microprofile-config.properties file)
    String keyvaultURL = config.getValue("azure.keyvault.url", String.class);

    // create the keyvault client
    SecretClient secretClient = new SecretClientBuilder().vaultUrl(keyvaultURL)
        .credential(new ManagedIdentityCredentialBuilder().build()).buildClient();

    keyVaultOperation = new AzureKeyVaultOperation(secretClient);
  }


  /**
   * Returns true if keyvault is disabled. Default is always false, which implies keyvault is
   * enabled by default
   *
   * @return
   */
  private boolean isKeyVaultDisabled(Config config) {
    this.configIsKeyVaultEnabled = config.getValue("azure.keyvault.enabled", String.class);
    return FALSE.equalsIgnoreCase(configIsKeyVaultEnabled);
  }

  @Override
  public Map<String, String> getProperties() {
    init();
    if (FALSE.equalsIgnoreCase(configIsKeyVaultEnabled)) {
      return new HashMap<>();
    }
    return keyVaultOperation.getProperties();
  }

  @Override
  public String getValue(String key) {
    init();
    if (FALSE.equalsIgnoreCase(configIsKeyVaultEnabled)) {
      return null;
    }
    return keyVaultOperation.getValue(key);
  }

  @Override
  public String getName() {
    return "AzureKeyVaultConfigSource";
  }

  @Override
  public Set<String> getPropertyNames() {
    init();
    if (FALSE.equalsIgnoreCase(configIsKeyVaultEnabled)) {
      return new HashSet<>();
    }
    return keyVaultOperation.getKeys();
  }

  @Override
  public int getOrdinal() {
    return 90;
  }
}
