//package com.microsoft.azure.microprofile.config.keyvault;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//import com.azure.security.keyvault.secrets.SecretClient;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//
//public class AzureKeyVaultOperationTest {
//
//
//  private static AzureKeyVaultOperation azureKeyVaultOperation;
//
//  @Mock
//  static SecretClient keyVaultClient;
//
//  @BeforeAll
//  static void setup() {
//    azureKeyVaultOperation = new AzureKeyVaultOperation(keyVaultClient, "keyvaulturi");
//  }
//
//  @DisplayName("Happy Flow to check getValue from KeyVault")
//  @Test
//  void getValueTest() {
//
//    assertEquals("retrievedsecret", azureKeyVaultOperation.getValue("somesecret"));
//  }
//
//
//}
