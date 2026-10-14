package com.digiview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }

            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                // Root redirection: Using any symbol like '@' or '~' or '/root' redirects directly to root '/'
                registry.addRedirectViewController("/@", "/");
                registry.addRedirectViewController("/@/", "/");
                registry.addRedirectViewController("/~", "/");
                registry.addRedirectViewController("/~/", "/");
                registry.addRedirectViewController("/root", "/");
                registry.addRedirectViewController("/root/", "/");
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // Static resources at root (e.g., /favicon.ico, /Splash-icon.png, /Icon.jpg)
                registry.addResourceHandler("/**")
                        .addResourceLocations(
                                "classpath:/static/",
                                "classpath:/public/",
                                "file:public/",
                                "file:../public/"
                        );

                // Dedicated Icons mapping
                registry.addResourceHandler("/Icons/**", "/icons/**")
                        .addResourceLocations(
                                "classpath:/static/Icons/",
                                "classpath:/static/icons/",
                                "classpath:/static/",
                                "file:public/Icons/",
                                "file:public/icons/",
                                "file:../public/Icons/",
                                "file:../public/icons/"
                        );

                // Dedicated Sounds mapping
                registry.addResourceHandler("/sounds/**", "/Sounds/**")
                        .addResourceLocations(
                                "classpath:/static/sounds/",
                                "classpath:/static/Sounds/",
                                "classpath:/static/",
                                "file:public/sounds/",
                                "file:public/Sounds/",
                                "file:../public/sounds/",
                                "file:../public/Sounds/"
                        );

                // Public prefix mapping
                registry.addResourceHandler("/public/**")
                        .addResourceLocations(
                                "classpath:/static/",
                                "file:public/",
                                "file:../public/"
                        );

                // Symbol '@' root mapping
                registry.addResourceHandler("/@/**")
                        .addResourceLocations(
                                "classpath:/static/",
                                "classpath:/static/Icons/",
                                "classpath:/static/sounds/",
                                "file:public/",
                                "file:../public/"
                        );
            }
        };
    }
}
