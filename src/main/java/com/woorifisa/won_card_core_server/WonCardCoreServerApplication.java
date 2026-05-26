package com.woorifisa.won_card_core_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WonCardCoreServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(WonCardCoreServerApplication.class, args);
	}

}