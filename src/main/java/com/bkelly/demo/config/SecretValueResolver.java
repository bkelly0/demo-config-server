package com.bkelly.demo.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.stereotype.Component;

@Component
final class SecretValueResolver {

  static final String SECRET_PREFIX = "sm://";

  private final SecretLookup secretLookup;

  SecretValueResolver(SecretLookup secretLookup) {
    this.secretLookup = secretLookup;
  }

  Environment resolve(Environment environment) {
    List<PropertySource> resolvedPropertySources =
        new ArrayList<>(environment.getPropertySources().size());
    for (PropertySource propertySource : environment.getPropertySources()) {
      resolvedPropertySources.add(resolvePropertySource(propertySource));
    }
    Environment resolved = new Environment(environment);
    resolved.addAll(resolvedPropertySources);
    return resolved;
  }

  private PropertySource resolvePropertySource(PropertySource propertySource) {
    Object source = propertySource.getSource();
    if (!(source instanceof Map<?, ?> properties)) {
      return propertySource;
    }

    Map<String, Object> resolvedProperties = new LinkedHashMap<>();
    properties.forEach(
        (key, value) -> resolvedProperties.put(String.valueOf(key), resolveValue(value)));
    return new PropertySource(propertySource.getName(), resolvedProperties);
  }

  private Object resolveValue(Object value) {
    if (value instanceof String stringValue) {
      return resolveStringValue(stringValue);
    }
    if (value instanceof Map<?, ?> nestedMap) {
      Map<String, Object> resolvedNestedMap = new LinkedHashMap<>();
      nestedMap.forEach(
          (key, nestedValue) ->
              resolvedNestedMap.put(String.valueOf(key), resolveValue(nestedValue)));
      return resolvedNestedMap;
    }
    if (value instanceof List<?> listValue) {
      List<Object> resolvedList = new ArrayList<>(listValue.size());
      for (Object nestedValue : listValue) {
        resolvedList.add(resolveValue(nestedValue));
      }
      return resolvedList;
    }
    return value;
  }

  private Object resolveStringValue(String value) {
    if (value.startsWith(SECRET_PREFIX)) {
      return secretLookup.resolve(value.substring(SECRET_PREFIX.length()));
    }
    return value;
  }
}
