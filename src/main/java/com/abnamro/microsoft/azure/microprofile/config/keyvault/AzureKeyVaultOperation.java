package com.abnamro.microsoft.azure.microprofile.config.keyvault;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


class AzureKeyVaultOperation {

  private static final long DEFAULT_CACHE_REFRESH_INTERVAL_IN_MS = 1800000L; // 30 minutes

  private final long cacheRefreshIntervalInMs; // 30 minutes

  private final SecretClient secretKeyVaultClient;

  private final Set<String> knownSecretKeys = new TreeSet<>();
  private final Map<String, String> propertiesMap = new ConcurrentHashMap<>();

  private final AtomicLong lastUpdateTime = new AtomicLong();
  private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

  AzureKeyVaultOperation() {
    this(DEFAULT_CACHE_REFRESH_INTERVAL_IN_MS);
  }
  AzureKeyVaultOperation(long cacheRefreshIntervalInMs) {
    this(cacheRefreshIntervalInMs, defaultSecretKeyVaultClient());
  }

  AzureKeyVaultOperation(long cacheRefreshIntervalInMs, SecretClient secretKeyVaultClient) {
    this.cacheRefreshIntervalInMs = cacheRefreshIntervalInMs;
    this.secretKeyVaultClient = secretKeyVaultClient;
  }

  private static SecretClient defaultSecretKeyVaultClient() {
    return new SecretClientBuilder().vaultUrl(getKeyVaultUrlFromConfig())
            .credential(new DefaultAzureCredentialBuilder().build()).buildClient();
  }

  private static String getKeyVaultUrlFromConfig() {
    return ConfigProvider.getConfig().getValue("azure.keyvault.url", String.class);
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
    if (System.currentTimeMillis() - lastUpdateTime.get() > cacheRefreshIntervalInMs) {
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
