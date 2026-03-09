package com.ctk.orchestrator.service;

import com.ctk.orchestrator.CtkProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component
public class DecisionClient {
  private final RestTemplate restTemplate;
  private final String decideUrl;
  private final ObjectMapper mapper;

  public DecisionClient(CtkProperties props, ObjectMapper mapper) {
    this.restTemplate = new RestTemplate();
    this.decideUrl = props.decisionServiceUrl() + "/decide";
    this.mapper = mapper;
  }

  public JsonNode decide(Map<String, Object> requestBody) {
    String payload;
    try {
      payload = mapper.writeValueAsString(requestBody);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize decision request", e);
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<>(payload, headers);

    try {
      String response = restTemplate.postForObject(decideUrl, entity, String.class);
      return mapper.readTree(response == null ? "{}" : response);
    } catch (Exception e) {
      throw new RuntimeException("Decision service call failed", e);
    }
  }
}
