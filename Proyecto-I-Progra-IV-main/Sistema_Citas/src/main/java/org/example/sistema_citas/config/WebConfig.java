package org.example.sistema_citas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Esto enlaza /images/perfiles/... con una carpeta externa en el disco
        registry.addResourceHandler("/images/perfiles/**")
                .addResourceLocations("file:///C:/sistema-citas/perfiles/");
    }
}
