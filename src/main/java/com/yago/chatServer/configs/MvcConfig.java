package com.yago.chatServer.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Clase de configuraciónn que permte el acceso a los archivos de la carpeta 'uploads' mediante una petición @GET
 * a la BBDD
 */

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadsDir = Paths.get(System.getProperty("user.dir"), "uploads").toFile().getAbsolutePath();

        registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + uploadsDir + "/");
    }
}