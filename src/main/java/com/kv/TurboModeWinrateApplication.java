package com.kv;

import com.kv.config.RestTemplateInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@SpringBootApplication
@Configuration
//@EnableSwagger2
public class TurboModeWinrateApplication {

    public static void main(String[] args) throws URISyntaxException, IOException {
        SpringApplication.run(TurboModeWinrateApplication.class, args);
    }

//    @Bean
//    public Docket getDocket() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                .select()
//                .apis(RequestHandlerSelectors.basePackage("com.kv"))
//                .paths(PathSelectors.any())
//                .build();
//    }

    @Bean
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(List.of(new RestTemplateInterceptor()));
        return restTemplate;
    }
}
