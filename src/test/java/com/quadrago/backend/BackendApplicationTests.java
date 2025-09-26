package com.quadrago.backend;

import com.quadrago.backend.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
		classes = BackendApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.MOCK,
		properties = {
				"spring.main.web-application-type=servlet"
		}
)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class BackendApplicationTests {
	@Test void contextLoads() { }
}
