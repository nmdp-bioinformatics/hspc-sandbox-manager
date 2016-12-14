package org.hspconsortium.sandboxmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

@SpringBootApplication
@ComponentScan({"org.hspconsortium"})
public class SandboxManagerApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SandboxManagerApplication.class);
    }

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(SandboxManagerApplication.class, args);
    }

    @Bean
    public UrlRewriteFilter getUrlRewriteFilter() {
        return new UrlRewriteFilter();
    }
}
