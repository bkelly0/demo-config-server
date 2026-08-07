package com.bkelly.demo.config;

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
final class GcpSecretLookup implements SecretLookup {

  private final ObjectProvider<SecretManagerServiceClient> secretManagerServiceClientProvider;
  private final String projectId;

  GcpSecretLookup(
      ObjectProvider<SecretManagerServiceClient> secretManagerServiceClientProvider,
      @Value("${gcp.project-id:}") String projectId) {
    this.secretManagerServiceClientProvider = secretManagerServiceClientProvider;
    this.projectId = projectId;
  }

  @Override
  public String resolve(String reference) {
    log.debug("Resolving secret: {}", reference);
    SecretManagerServiceClient client = secretManagerServiceClientProvider.getObject();
    String resourceName = toResourceName(reference);
    return client.accessSecretVersion(resourceName).getPayload().getData().toStringUtf8();
  }

  private String toResourceName(String reference) {
    if (reference.startsWith("projects/")) {
      if (reference.contains("/versions/")) {
        return reference;
      }
      return reference + "/versions/latest";
    }

    if (!StringUtils.hasText(projectId)) {
      throw new IllegalStateException(
          "gcp.project-id must be set to resolve sm:// secret references");
    }

    return SecretVersionName.of(projectId, reference, "latest").toString();
  }
}
