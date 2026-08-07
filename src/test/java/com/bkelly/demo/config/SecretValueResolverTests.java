package com.bkelly.demo.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;

class SecretValueResolverTests {

  @Test
  void resolvesSmReferencesInPropertySources() {
    SecretValueResolver resolver = new SecretValueResolver(reference -> "resolved:" + reference);

    Environment environment = new Environment("demo", "default", "main");
    Map<String, Object> properties = new LinkedHashMap<>();
    properties.put("username", "alice");
    properties.put("password", "sm://db-password");
    properties.put("nested", List.of("sm://api-key", "plain"));
    environment.getPropertySources().add(new PropertySource("git", properties));

    Environment resolved = resolver.resolve(environment);
    Map<?, ?> resolvedProperties = (Map<?, ?>) resolved.getPropertySources().get(0).getSource();

    assertThat(resolvedProperties.get("username")).isEqualTo("alice");
    assertThat(resolvedProperties.get("password")).isEqualTo("resolved:db-password");
    assertThat(resolvedProperties.get("nested")).isEqualTo(List.of("resolved:api-key", "plain"));
  }
}
