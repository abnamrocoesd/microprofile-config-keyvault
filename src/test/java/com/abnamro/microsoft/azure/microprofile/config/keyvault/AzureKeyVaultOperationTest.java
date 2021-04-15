package com.abnamro.microsoft.azure.microprofile.config.keyvault;

import com.abnamro.microsoft.azure.microprofile.config.keyvault.util.MockAzureKeyVaultClient;
import com.azure.core.http.HttpClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AzureKeyVaultOperationTest {

    @Mock
    private HttpClient httpClient;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }
    @Test
    void getSameValue() {
        AzureKeyVaultOperation azureKeyVaultOperation = new AzureKeyVaultOperation(0L, MockAzureKeyVaultClient.simpleSecretClientMock(httpClient));
        KeyVaultSecret keyVaultSecret1 = new KeyVaultSecret("secret1","value1");
        List<KeyVaultSecret> keyVaultSecrets = Arrays.asList(keyVaultSecret1);

        MockAzureKeyVaultClient.simpleAzureKeyvaultMock(keyVaultSecrets, httpClient);

        azureKeyVaultOperation.getValue("secret1");

        IntStream.range(1, 5).mapToObj(x -> "secret1")
                .map(azureKeyVaultOperation::getValue)
                .forEach(value -> assertEquals("value1", value));
    }

    @Test
    void getSameValueInParallel() {
        AzureKeyVaultOperation azureKeyVaultOperation = new AzureKeyVaultOperation(0L, MockAzureKeyVaultClient.simpleSecretClientMock(httpClient));
        KeyVaultSecret keyVaultSecret1 = new KeyVaultSecret("secret1","value1");
        List<KeyVaultSecret> keyVaultSecrets = Arrays.asList(keyVaultSecret1);

        MockAzureKeyVaultClient.simpleAzureKeyvaultMock(keyVaultSecrets, httpClient);

        azureKeyVaultOperation.getValue("secret1");

        IntStream.range(1, 5).mapToObj(x -> "secret1").parallel()
                .map(azureKeyVaultOperation::getValue)
                .forEach(value -> assertEquals("value1", value));
    }
}