package br.com.danieloliveira.jdbcwithspring;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import br.com.danieloliveira.jdbcwithspring.core.model.Customer;
import br.com.danieloliveira.jdbcwithspring.core.model.mapper.CustomerRowMapper;
import br.com.danieloliveira.jdbcwithspring.infra.CustomSQLErrorCodeTranslator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class JdbcwithspringApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(JdbcwithspringApplication.class, args);
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void run(String... strings) throws Exception {
        CustomSQLErrorCodeTranslator customSQLErrorCodeTranslator = new CustomSQLErrorCodeTranslator();
        jdbcTemplate.setExceptionTranslator(customSQLErrorCodeTranslator);

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
                    rs.getInt("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name")),
                "Josh")
                .forEach(customer -> log.info(customer.toString()));


        log.info("Listando todos");

        jdbcTemplate.query("SELECT id, first_name, last_name FROM customers",
                (rs, rowNum) -> new Customer(
                    rs.getInt("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name")))
                .forEach(customer -> log.info(customer.toString()));

        @SuppressWarnings("null")
        int result = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class);
        log.info("Count customers: {}", result);


        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("id", 1);
        String firstName = namedParameterJdbcTemplate.queryForObject("SELECT first_name FROM customers WHERE ID = :id", namedParameters, String.class);
        log.info("First name do ID 1: {}", firstName);


        Customer customer = new Customer();
        customer.setFirstName("Josh");
        String selectByName = "SELECT COUNT(*) FROM customers WHERE first_name = :firstName";
        namedParameters = new BeanPropertySqlParameterSource(customer);
        @SuppressWarnings("null")
        int resultCount = namedParameterJdbcTemplate.queryForObject(selectByName, namedParameters, Integer.class);
        log.info("Count by firstName Josh: {}", resultCount);


        String query = "SELECT * FROM customers WHERE ID = ?";
        Customer customerRowMapper = jdbcTemplate.queryForObject(query, new CustomerRowMapper(), 2);
        log.info("Customer id 2: {}", customerRowMapper);

        Customer customerSimpleInsert = new Customer();
        customerSimpleInsert.setId(100);
        customerSimpleInsert.setFirstName("Daniel");
        customerSimpleInsert.setLastName("Oliveira");
        int insertCount = addCustomer(customerSimpleInsert, jdbcTemplate.getDataSource());
        log.info("Dados inseridos simpleJdbcInsert: {}", insertCount);
        log.info("Customer id 100: {}", customerById(jdbcTemplate, 100));


        Customer customerSimpleInsertExecute = new Customer();
        customerSimpleInsertExecute.setFirstName("Maria");
        customerSimpleInsertExecute.setLastName("Minha Gata");
        Number idCustomer = addCustomerExecuteAndReturnKey(customerSimpleInsertExecute, jdbcTemplate.getDataSource());
        log.info("Add customer id return: {}", idCustomer);


        List<Customer> lista = new ArrayList<>();
        lista.add(new Customer("Kieth", "Clark"));
        lista.add(new Customer("Anthony", "Leigh"));
        lista.add(new Customer("Deborah", "Everett"));
        lista.add(new Customer("Russel", "Gilbert"));
        log.info("BatchUpdateUsingJdbcTemplate... total: {}", batchUpdateUsingJdbcTemplate(jdbcTemplate, lista));

        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(lista.toArray());
        int[] updateCounts = namedParameterJdbcTemplate.batchUpdate("INSERT INTO customers(first_name, last_name) VALUES (:firstName, :lastName)", batch);
        log.info("BatchUpdate... total: {}", updateCounts);
    }

    public static int addCustomer(Customer obj, DataSource dataSource) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("customers");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", obj.getId());
        parameters.put("first_name", obj.getFirstName());
        parameters.put("last_name", obj.getLastName());

        return simpleJdbcInsert.execute(parameters);
    }
    
    public static Number addCustomerExecuteAndReturnKey(Customer obj, DataSource dataSource) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("customers").usingGeneratedKeyColumns("id");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", obj.getId());
        parameters.put("first_name", obj.getFirstName());
        parameters.put("last_name", obj.getLastName());

        return simpleJdbcInsert.executeAndReturnKey(parameters);
    }

    public static Customer customerById(JdbcTemplate jdbcTemplate, int id) {
        String query = "SELECT * FROM customers WHERE ID = ?";
        return jdbcTemplate.queryForObject(query, new CustomerRowMapper(), id);
    }

    public static int[] batchUpdateUsingJdbcTemplate(JdbcTemplate jdbcTemplate, List<Customer> listaEntities) {
        return jdbcTemplate.batchUpdate("INSERT INTO customers(first_name, last_name) VALUES (?, ?)",
            new BatchPreparedStatementSetter() {
                @SuppressWarnings("null")
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setString(1, listaEntities.get(i).getFirstName());
                    ps.setString(2, listaEntities.get(i).getLastName());
                }
                @Override
                public int getBatchSize() {
                    return listaEntities.size();
                }
            });
    }
}
