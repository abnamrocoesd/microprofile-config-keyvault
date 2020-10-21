package com.microsoft.azure.microprofile.config.keyvault;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class AzureKeyVaultConfigSourceTest {

  @Mock
  AzureKeyVaultOperation keyVaultOperation;
  @InjectMocks
  AzureKeyVaultConfigSource azureKeyVaultConfigSource;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void testGetProperties() {
    when(keyVaultOperation.getProperties()).thenReturn(new HashMap<String, String>() {{
      put("String", "String");
    }});

    Map<String, String> result = azureKeyVaultConfigSource.getProperties();
    Assertions.assertEquals(new HashMap<String, String>() {{
      put("String", "String");
    }}, result);
  }

  @Test
  void testGetValue() {
    when(keyVaultOperation.getValue(anyString())).thenReturn("getValueResponse");

    String result = azureKeyVaultConfigSource.getValue("key");
    Assertions.assertEquals("getValueResponse", result);
  }

  @Test
  void testGetName() {
    String result = azureKeyVaultConfigSource.getName();
    Assertions.assertEquals("AzureKeyVaultConfigSource", result);
  }

  @Test
  void testGetPropertyNames() {
    when(keyVaultOperation.getKeys()).thenReturn(new HashSet<>(Arrays.asList("String")));

    Set<String> result = azureKeyVaultConfigSource.getPropertyNames();
    Assertions.assertEquals(new HashSet<String>(Arrays.asList("String")), result);
  }

  @Test
  void testGetOrdinal() {
    int result = azureKeyVaultConfigSource.getOrdinal();
    Assertions.assertEquals(90, result);
  }
}
