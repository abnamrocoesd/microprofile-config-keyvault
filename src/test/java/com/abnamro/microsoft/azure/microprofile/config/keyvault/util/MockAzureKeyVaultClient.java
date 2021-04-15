package com.abnamro.microsoft.azure.microprofile.config.keyvault.util;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Page;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.IterableStream;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class MockAzureKeyVaultClient {

    public static final int STATUS_CODE_OK = 200;

    private MockAzureKeyVaultClient() {}

    public static HttpClient simpleAzureKeyvaultMock(Collection<KeyVaultSecret> keyVaultSecrets) {
        return simpleAzureKeyvaultMock(keyVaultSecrets, Mockito.mock(HttpClient.class));
    }

    public static SecretClient simpleSecretClientMock(HttpClient httpClient) {
        HttpPipeline mockPipeline = new HttpPipelineBuilder().httpClient(httpClient).build();
        return new SecretClientBuilder()
                .pipeline(mockPipeline).vaultUrl("https://localhost")
                .buildClient();
    }

    public static HttpClient simpleAzureKeyvaultMock(Collection<KeyVaultSecret> keyVaultSecrets, HttpClient httpClient) {
        mockHttpClientResponse(keyVaultSecrets, httpClient);
        return httpClient;
    }

    private static <T> void mockHttpClientResponse(Collection<KeyVaultSecret> keyVaultSecrets, HttpClient httpClient) {

        Map<String, Object> urlToResponseMap = urlToResponseMapFromSecrets(keyVaultSecrets);
        when(httpClient.send(any())).thenAnswer(invocationOnMock -> {
            String url = invocationOnMock.getArgumentAt(0, HttpRequest.class).getUrl().getPath();
            byte[] serialized = new ObjectMapper().writer().writeValueAsBytes(urlToResponseMap.get(url));
            HttpRequest request = invocationOnMock.getArgumentAt(0, HttpRequest.class);
            return Mono.just(new MockHttpResponse(request, STATUS_CODE_OK, serialized));
        });
    }

    @NotNull
    private static Map<String, Object> urlToResponseMapFromSecrets(Collection<KeyVaultSecret> keyVaultSecrets) {
        SecretPropertiesPageMock secretPropertiesPageMock = new SecretPropertiesPageMock();
        secretPropertiesPageMock.items = keyVaultSecrets.stream()
                .map(KeyVaultSecret::getProperties)
                .collect(Collectors.toList());
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("/secrets", secretPropertiesPageMock);
        keyVaultSecrets.forEach(secret -> objectMap.put(String.format("/secrets/%s/", secret.getName()),secret));
        return objectMap;
    }

    public static final class SecretPropertiesPageMock implements Page<SecretProperties> {
        @JsonProperty("nextLink")
        public String continuationToken;
        @JsonProperty("value")
        public List<SecretProperties> items;

        public SecretPropertiesPageMock() {
        }

        public String getContinuationToken() {
            return this.continuationToken;
        }

        public IterableStream<SecretProperties> getElements() {
            return IterableStream.of(this.items);
        }
    }
}
