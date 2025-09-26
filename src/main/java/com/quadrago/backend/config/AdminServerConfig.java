package com.quadrago.backend.config;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test") // Admin Server só fora do profile de teste
@EnableAdminServer
public class AdminServerConfig {
}
