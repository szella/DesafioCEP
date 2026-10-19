package br.com.szella.desafiocep.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Autowired(required = false)
    private BuildProperties buildProperties;

    @Bean
    public OpenAPI customOpenAPI() {
        String title = (buildProperties != null) ? buildProperties.getName() : "Desafio CEP";
        String version = (buildProperties != null) ? buildProperties.getVersion() : "DEV-SNAPSHOT";

        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .version(version)
                        .description("API RESTful para busca de CEP.")
                        .contact(new Contact()
                                .name("Sérgio Luiz Amorim Zella")
                                .email("sergioluiz.zella@gmail.com")
                        )
                );
    }
}
