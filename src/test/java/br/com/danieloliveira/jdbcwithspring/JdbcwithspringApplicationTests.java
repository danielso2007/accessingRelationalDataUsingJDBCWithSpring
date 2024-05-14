package br.com.danieloliveira.jdbcwithspring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class JdbcwithspringApplicationTests extends AbstractIntegrationTest {

	@Test
	void contextLoads() {
	}

}
