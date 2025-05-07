package com.inmobi.guessinggame;

import com.inmobi.guessinggame.config.GameConfig;
import com.inmobi.guessinggame.config.JwtConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableConfigurationProperties({JwtConfig.class, GameConfig.class})
public class GuessinggameApplication {

	public static void main(String[] args) {
		SpringApplication.run(GuessinggameApplication.class, args);
	}
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
