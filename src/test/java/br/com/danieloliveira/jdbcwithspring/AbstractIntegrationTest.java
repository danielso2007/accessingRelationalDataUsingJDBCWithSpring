package br.com.danieloliveira.jdbcwithspring;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(classes = JdbcwithspringApplicationTests.class, webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:tc:postgres:16.2-alpine3.19:///jdbcwithspring" })
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {
    static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16.2-alpine3.19")
            .withDatabaseName("jdbcwithspring").withUsername("postgres").withPassword("postgres");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        postgresContainer.start();
        registry.add("DB_HOST", postgresContainer::getHost);
        registry.add("DB_PORT", postgresContainer::getFirstMappedPort);
        registry.add("DB_USER", postgresContainer::getUsername);
        registry.add("DB_PASSWORD", postgresContainer::getPassword);
        registry.add("DATA_BASE", postgresContainer::getDatabaseName);
    }
}