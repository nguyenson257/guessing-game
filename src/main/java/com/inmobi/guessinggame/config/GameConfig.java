package com.inmobi.guessinggame.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "game")
public class GameConfig {
    private double winRate;
    private int turns;
}
