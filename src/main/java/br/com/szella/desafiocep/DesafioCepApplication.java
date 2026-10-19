package br.com.szella.desafiocep;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DesafioCepApplication {

	public static void main(String[] args) {
		SpringApplication.run(DesafioCepApplication.class, args);
	}

}
