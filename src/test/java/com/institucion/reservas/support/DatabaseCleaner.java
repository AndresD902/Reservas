package com.institucion.reservas.support;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseCleaner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void clean() {
        jdbcTemplate.update("DELETE FROM reservas");
        jdbcTemplate.update("DELETE FROM recursos");
        jdbcTemplate.update("DELETE FROM usuarios");
        jdbcTemplate.update("DELETE FROM administradores");
    }
}
