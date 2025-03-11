package com.aoc.zerodowntime.config;

import org.jline.terminal.Terminal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.ResultHandler;
import org.springframework.shell.result.DefaultResultHandler;

@Configuration
public class ShellConfig {

    @Bean
    public ResultHandler<?> resultHandler(Terminal terminal) {
        return new DefaultResultHandler(terminal);
    }
}
