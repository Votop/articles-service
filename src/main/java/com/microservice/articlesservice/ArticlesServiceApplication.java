package com.microservice.articlesservice;

import com.microservice.articlesservice.web.controller.ArticleController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@ComponentScan("com.microservice")
public class ArticlesServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArticlesServiceApplication.class, args);
	}

}
