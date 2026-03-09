package com.ctk.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CtkProperties.class)
public class CtkOrchestratorApplication {
  public static void main(String[] args) {
    SpringApplication.run(CtkOrchestratorApplication.class, args);
  }
}
