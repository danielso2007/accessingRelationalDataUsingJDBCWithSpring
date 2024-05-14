package br.com.danieloliveira.jdbcwithspring.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Customer {
    private long id;
    private String firstName;
    private String lastName;
}
