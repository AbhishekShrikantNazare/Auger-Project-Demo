package com.ctk.orchestrator.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class IdempotencyService {
  private final StringRedisTemplate redis;

  public IdempotencyService(StringRedisTemplate redis) {
    this.redis = redis;
  }

  /** Returns true if this eventId was newly claimed, false if already seen. */
  public boolean claimEvent(String eventId) {
    String key = "event:" + eventId;
    Boolean ok = redis.opsForValue().setIfAbsent(key, "1", Duration.ofHours(24));
    return ok != null && ok;
  }
}
