package com.microsoft.azure.microprofile.config.keyvault;

import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.Map;
import java.util.Set;

public class AzureKeyVaultConfigSource implements ConfigSource {

  private AzureKeyVaultOperation keyVaultOperation;

  public AzureKeyVaultConfigSource() {
    // no-op
  }

  private void init() {
      if (keyVaultOperation != null) {
          return;
      }

    // read in keyvault config settings from external config source (normally the environment or a microprofile-config.properties file)
    Config config = ConfigProvider.getConfig();
    String keyvaultURL = config.getValue("azure.keyvault.url", String.class);

    // create the keyvault client
    SecretClient secretClient = new SecretClientBuilder().vaultUrl(keyvaultURL)
        .credential(new ManagedIdentityCredentialBuilder().build()).buildClient();

    keyVaultOperation = new AzureKeyVaultOperation(secretClient, keyvaultURL);
  }

  @Override
  public Map<String, String> getProperties() {
    init();
    return keyVaultOperation.getProperties();
  }

  @Override
  public String getValue(String key) {
    init();
    return keyVaultOperation.getValue(key);
  }

  @Override
  public String getName() {
    return "AzureKeyVaultConfigSource";
  }

  @Override
  public Set<String> getPropertyNames() {
    init();
    return keyVaultOperation.getKeys();
  }

  @Override
  public int getOrdinal() {
    return 90;
  }
}
