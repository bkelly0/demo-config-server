package com.bkelly.demo.config;

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import java.io.IOException;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.cloud.config.server.environment.MultipleJGitEnvironmentProperties;
import org.springframework.cloud.config.server.environment.MultipleJGitEnvironmentRepositoryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
class ConfigServerSecretResolutionConfiguration {

  @Bean
  EnvironmentRepository secretResolvingEnvironmentRepository(
      MultipleJGitEnvironmentRepositoryFactory gitEnvironmentRepositoryFactory,
      MultipleJGitEnvironmentProperties environmentProperties,
      SecretValueResolver secretValueResolver)
      throws Exception {
    EnvironmentRepository delegate = gitEnvironmentRepositoryFactory.build(environmentProperties);
    return (application, profile, label) ->
        secretValueResolver.resolve(delegate.findOne(application, profile, label));
  }

  @Bean
  @Lazy
  SecretManagerServiceClient secretManagerServiceClient() throws IOException {
    return SecretManagerServiceClient.create();
  }
}
