package com.microsoft.azure.microprofile.config.keyvault;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class AzureKeyVaultOperation {

  private static final long CACHE_REFRESH_INTERVAL_IN_MS = 1800000L; // 30 minutes

  private final SecretClient keyVaultClient;
  private final String vaultUri;

  private final Set<String> knownSecretKeys;
  private final Map<String, String> propertiesMap;

  private final AtomicLong lastUpdateTime = new AtomicLong();
  private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

  AzureKeyVaultOperation(SecretClient keyVaultClient, String vaultUri) {
    this.keyVaultClient = keyVaultClient;
    this.propertiesMap = new ConcurrentHashMap<>();
    this.knownSecretKeys = new TreeSet<>();

    vaultUri = vaultUri.trim();
    if (vaultUri.endsWith("/")) {
      vaultUri = vaultUri.substring(0, vaultUri.length() - 1);
    }
    this.vaultUri = vaultUri;

    createOrUpdateHashMap();
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
          .computeIfAbsent(secretName, key -> keyVaultClient.getSecret(vaultUri, key).getValue());
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

      keyVaultClient.listPropertiesOfSecrets()
          .stream()
          .map(SecretProperties::getName)
          .forEach(knownSecretKeys::add);
      lastUpdateTime.set(System.currentTimeMillis());

    } finally {
      rwLock.writeLock().unlock();
    }
  }
}
