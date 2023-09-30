package dev.tarcisio.minebox.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {
  @Value("${swagger.dev-url}")
  private String devUrl;

  @Bean
  public OpenAPI myOpenApi() {
    Server devServer = new Server();
    devServer.setUrl(devUrl);
    devServer.setDescription("Server URL in Development enviroment");

    Contact contact = new Contact();
    contact.setEmail("tarcisiodelmondes@gmail.com");
    contact.setName("Tarcisio");
    contact.setUrl("https://minebox.tarcisiodelmondes.dev");

    License mitLicense = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");

    Info info = new Info().title("Minebox API").version("1.0").contact(contact).license(mitLicense);

    return new OpenAPI().info(info).servers(List.of(devServer));
  }
}
