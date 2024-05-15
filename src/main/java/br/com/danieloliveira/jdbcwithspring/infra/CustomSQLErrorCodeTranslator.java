package br.com.danieloliveira.jdbcwithspring.infra;

import java.sql.SQLException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

public class CustomSQLErrorCodeTranslator extends SQLErrorCodeSQLExceptionTranslator {
    @Override
    @SuppressWarnings("null")
    protected DataAccessException customTranslate(String task, String sql, SQLException sqlException) {
        if (sqlException.getErrorCode() == 23505) {
            return new DuplicateKeyException("Custom Exception translator - Integrity constraint violation.", sqlException);
        }
        return null;
    }
}
