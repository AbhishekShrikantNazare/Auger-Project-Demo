package com.ctk.orchestrator;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ctk")
public record CtkProperties(String decisionServiceUrl) {}
