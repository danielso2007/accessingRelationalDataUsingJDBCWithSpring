package br.com.danieloliveira.jdbcwithspring.core.model.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;
import br.com.danieloliveira.jdbcwithspring.core.model.Customer;

public class CustomerRowMapper implements RowMapper<Customer> {
    @Override
    @Nullable
    public Customer mapRow(@SuppressWarnings("null") ResultSet rs, int rowNum) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getInt("id"));
        customer.setFirstName(rs.getString("first_name"));
        customer.setLastName(rs.getString("last_name"));
        return customer;
    }
}
