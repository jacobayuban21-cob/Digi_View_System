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
                // Root folder mapping for public assets and symbol-based paths (@/public, @/Icons, @/sounds)
                registry.addResourceHandler("/@/**", "/public/**", "/Icons/**", "/sounds/**")
                        .addResourceLocations(
                                "file:public/",
                                "file:../public/",
                                "file:../../public/",
                                "classpath:/static/",
                                "classpath:/public/"
                        );
            }
        };
    }
}
