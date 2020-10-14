# Azure Key Vault Config Source for MicroProfile Config API

This project contains a config source for the MicroProfile Config API. It allows for dependency injection of values from Azure Key Vault into MicroProfile-based microservices.

## Using the Azure Key Vault Config Source for MicroProfile Config API

Firstly, add the dependency on this library to your project. Below are examples of how to do this in Maven and Gradle:

**Maven:**

```xml
<dependency>
    <groupId>com.abnamro.coesd.azure</groupId>
    <artifactId>microprofile-config-keyvault</artifactId>
    <version>0.0.20</version>
</dependency>
```

**Gradle:**

```gradle
compile group: 'com.abnamro.coesd.azure', name: 'microprofile-config-keyvault', version: '1.0.0'
```

After adding the dependency to your MicroProfile-based application, create a `microprofile-config.properties` file inside `src/main/resources/META-INF`. Inside this file, the following properties must be set based on the Azure Key Vault configuration you have created (if you haven't created an Azure Key Vault yet, refer to the [steps in my other repo](https://github.com/Azure-Samples/microprofile-configsource-keyvault)):

```properties
# is the key vault name specified in step two, e.g. 'microprofile-kv'
azure.keyvault.url=https://cicd-kv.vault.azure.net
azure.keyvault.enabled=true
# NOTE that, by default azure.keyvault.enabled is always true, if for local you want to disable it, make it false
```

Save this file, and then we can move on to your REST endpoints. Again, the [keyvault-configsource sample app](https://github.com/Azure-Samples/microprofile-configsource-keyvault) has good reference code, but I have reproduced it below:

```java
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.TEXT_HTML;

@ApplicationScoped
@Path("/")
public class ConfigDemo {

    @Inject
    org.eclipse.microprofile.config.Config config;

    @Inject
    @ConfigProperty(name = "demo-key", defaultValue = "Unknown")
    String demoKeyValue;

    @GET
    @Path("config")
    @Produces(TEXT_HTML)
    public String info() {
        return "Welcome to the server! I know that the value for the key 'demo-key' is: '" + demoKeyValue
                + "'<br/><br/>"
                + "By the way, I can also look it up in a non-DI fashion: '" + config.getValue("demo-key", String.class) + "'";
    }
}
```
