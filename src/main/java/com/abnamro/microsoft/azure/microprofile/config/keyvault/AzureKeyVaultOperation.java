package com.abnamro.microsoft.azure.microprofile.config.keyvault;

import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.eclipse.microprofile.config.ConfigProvider;


class AzureKeyVaultOperation {

  private static final long CACHE_REFRESH_INTERVAL_IN_MS = 1800000L; // 30 minutes

  private final SecretClient secretKeyVaultClient;

  private final Set<String> knownSecretKeys = new TreeSet<>();
  private final Map<String, String> propertiesMap = new ConcurrentHashMap<>();

  private final AtomicLong lastUpdateTime = new AtomicLong();
  private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

  AzureKeyVaultOperation() {
    String keyVaultURL = ConfigProvider.getConfig().getValue("azure.keyvault.url", String.class);
    this.secretKeyVaultClient = new SecretClientBuilder().vaultUrl(keyVaultURL)
        .credential(new ManagedIdentityCredentialBuilder().build()).buildClient();
  }

  Set<String> getKeys() {
    checkRefreshTimeOut();

    try {
      rwLock.readLock().lock();
      return propertiesMap.keySet();
    } finally {
      rwLock.readLock().unlock();
    }
  }

  Map<String, String> getProperties() {
    checkRefreshTimeOut();

    try {
      rwLock.readLock().lock();
      return Collections.unmodifiableMap(propertiesMap);
    } finally {
      rwLock.readLock().unlock();
    }
  }

  String getValue(String secretName) {
    checkRefreshTimeOut();

    if (knownSecretKeys.contains(secretName)) {
      return propertiesMap
          .computeIfAbsent(secretName,
              key -> secretKeyVaultClient.getSecret(secretName).getValue());
    }

    return null;
  }

  private void checkRefreshTimeOut() {
    // refresh periodically
    if (System.currentTimeMillis() - lastUpdateTime.get() > CACHE_REFRESH_INTERVAL_IN_MS) {
      lastUpdateTime.set(System.currentTimeMillis());
      createOrUpdateHashMap();
    }
  }

  private void createOrUpdateHashMap() {
    try {
      rwLock.writeLock().lock();
      propertiesMap.clear();
      knownSecretKeys.clear();

      secretKeyVaultClient.listPropertiesOfSecrets()
          .stream()
          .map(SecretProperties::getName)
          .forEach(knownSecretKeys::add);
      lastUpdateTime.set(System.currentTimeMillis());

    } finally {
      rwLock.writeLock().unlock();
    }
  }

}
