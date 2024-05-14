package br.com.danieloliveira.jdbcwithspring;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import br.com.danieloliveira.jdbcwithspring.core.model.Customer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class JdbcwithspringApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(JdbcwithspringApplication.class, args);
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... strings) throws Exception {

        log.info("Creating tables");

        jdbcTemplate.execute("DROP TABLE IF EXISTS customers");
        jdbcTemplate.execute("CREATE TABLE customers(id SERIAL, first_name VARCHAR(255), last_name VARCHAR(255))");

        // Divida a matriz de nomes inteiros em uma matriz de nomes/sobrenomes
        List<Object[]> splitUpNames = Arrays.asList("John Woo", "Jeff Dean", "Josh Bloch", "Josh Long").stream().map(name -> name.split(" ")).collect(Collectors.toList());
        
        // Use um fluxo Java 8 para imprimir cada tupla da lista
        splitUpNames.forEach(name -> log.info(String.format("Inserting customer record for %s %s", name[0], name[1])));

        // Usa a operação batchUpdate do JdbcTemplate para carregar dados em massa
        jdbcTemplate.batchUpdate("INSERT INTO customers(first_name, last_name) VALUES (?,?)", splitUpNames);

        log.info("Querying for customer records where first_name = 'Josh':");

        jdbcTemplate.query("SELECT id, first_name, last_name FROM customers WHERE first_name = ?",
                (rs, rowNum) -> new Customer(
                    rs.getLong("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name")),
                "Josh")
                .forEach(customer -> log.info(customer.toString()));
    }
}
