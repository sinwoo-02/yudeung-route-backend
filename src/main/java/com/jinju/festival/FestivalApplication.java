package com.jinju.festival;

import com.jinju.festival.global.config.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FestivalApplication {

	public static void main(String[] args) {
		DotenvLoader.load();
		SpringApplication.run(FestivalApplication.class, args);
	}

}
