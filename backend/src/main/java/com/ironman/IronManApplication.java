package com.ironman;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class IronManApplication {

	public static void main(String[] args) {
		SpringApplication.run(IronManApplication.class, args);
	}
}